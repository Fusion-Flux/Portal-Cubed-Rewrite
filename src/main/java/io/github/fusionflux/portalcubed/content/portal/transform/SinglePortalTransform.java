package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.framework.entity.LerpableEntity;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.LivingEntityAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class SinglePortalTransform implements PortalTransform {
	public static final StreamCodec<ByteBuf, SinglePortalTransform> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, transform -> transform.inOrigin,
			ByteBufCodecs.QUATERNIONF, transform -> transform.inRot,
			Vec3.STREAM_CODEC, transform -> transform.outOrigin,
			ByteBufCodecs.QUATERNIONF, transform -> transform.outRot,
			SinglePortalTransform::new
	);

	public static final Vec3 UP = new Vec3(0, 1, 0);

	public final Vec3 inOrigin;
	public final Quaternionfc inRot;
	public final Quaternionfc inRotInverse;
	public final Quaternionfc inRot180;

	public final Vec3 outOrigin;
	public final Quaternionfc outRot;
	public final Quaternionfc outRot180;

	private final SinglePortalTransform inverse;

	public SinglePortalTransform(Portal in, Portal out) {
		this(in.origin(), in.data.rotation(), out.origin(), out.data.rotation());
	}

	public SinglePortalTransform(Vec3 inOrigin, Quaternionfc inRot, Vec3 outOrigin, Quaternionfc outRot) {
		this(inOrigin, inRot, outOrigin, outRot, null);
	}

	private SinglePortalTransform(Vec3 inOrigin, Quaternionfc inRot, Vec3 outOrigin, Quaternionfc outRot, @Nullable SinglePortalTransform inverse) {
		this.inOrigin = inOrigin;
		this.inRot = new Quaternionf(inRot);
		this.inRotInverse = inRot.invert(new Quaternionf());
		this.inRot180 = rotate180(inRot);
		this.outOrigin = outOrigin;
		this.outRot = new Quaternionf(outRot);
		this.outRot180 = rotate180(outRot);
		this.inverse = inverse != null ? inverse : new SinglePortalTransform(outOrigin, outRot, inOrigin, inRot, this);
	}

	@Override
	public SinglePortalTransform inverse() {
		return this.inverse;
	}

	@Override
	public MultiPortalTransform andThen(PortalTransform next) {
		List<SinglePortalTransform> steps = new ArrayList<>();
		steps.add(this);
		next.forEachStep(steps::add);
		return new MultiPortalTransform(steps);
	}

	@Override
	public void forEachStep(Consumer<SinglePortalTransform> consumer) {
		consumer.accept(this);
	}

	@Override
	public Vector3d applyRelative(Vector3d pos) {
		this.inRotInverse.transform(pos);
		this.outRot180.transform(pos);
		return pos;
	}

	@Override
	public Vector3d applyAbsolute(Vector3d pos) {
		pos.sub(this.inOrigin.x, this.inOrigin.y, this.inOrigin.z);
		this.applyRelative(pos);
		return pos.add(this.outOrigin.x, this.outOrigin.y, this.outOrigin.z);
	}

	@Override
	public Matrix3d apply(Matrix3d rotation) {
		return rotation.rotateLocal(this.inRotInverse).rotateLocal(this.outRot180);
	}

	@Override
	public Quaternionf apply(Quaternionf rotation) {
		return rotation.premul(this.inRotInverse).premul(this.outRot180);
	}

	@Override
	public Rotations apply(Rotations rotations) {
		// TODO: handle Z
		Quaternionf rot = new Quaternionf()
				.rotationYXZ((180 - rotations.getY()) * Mth.DEG_TO_RAD, -rotations.getX() * Mth.DEG_TO_RAD, 0)
				.premul(this.inRotInverse)
				.premul(this.outRot180)
				.conjugate();
		float pitch = (float) Math.atan2((rot.x * rot.w + rot.y * rot.z) * 2, 1 - 2 * (rot.x * rot.x + rot.z * rot.z));
		float yaw = (float) Math.atan2(-(rot.x * rot.z + rot.y * rot.w) * 2, 2 * (rot.y * rot.y + rot.z * rot.z) - 1);
		return new Rotations(pitch * Mth.RAD_TO_DEG, yaw * Mth.RAD_TO_DEG, 0);
	}

	@Override
	public void apply(Entity entity) {
		Vec3 pos = entity.position();
		Vec3 center = PortalTeleportHandler.centerOf(entity);
		Vec3 posToCenter = pos.vectorTo(center);
		Vec3 centerToPos = center.vectorTo(pos);

		// grab these before doing anything
		boolean wasGrounded = entity.onGround();
		Vec3 oldPos = entity.oldPosition();

		// teleport
		Vec3 newPos = this.applyAbsolute(center).add(centerToPos);
		entity.setPos(newPos);

		if (entity instanceof ServerPlayer player) {
			// this needs to be done manually for some reason.
			// vanilla calls this in places where the player normally moves, instead of just doing it automatically.
			// this might cause chunk tracking to be incorrect in some cases, but I don't care enough to investigate, not my bug.
			player.serverLevel().getChunkSource().move(player);
		}

		// rotate
		Rotations newRotations = this.apply(entity.getXRot(), entity.getYRot());
		entity.setXRot(newRotations.getX());
		entity.setYRot(newRotations.getY());

		if (entity instanceof LivingEntity living) {
			// we explicitly do not want to call setters here even when they're available, because side effects can ruin our day.
			// for example, armor stands try to keep a bunch of rotation fields in sync, which will overwrite stuff.
			living.yHeadRot = this.apply(living.yHeadRot, Direction.Axis.Y);
			living.yBodyRot = this.apply(living.yBodyRot, Direction.Axis.Y);
			living.yHeadRotO = this.apply(living.yHeadRotO, Direction.Axis.Y);
			living.yBodyRotO = this.apply(living.yBodyRotO, Direction.Axis.Y);

			LivingEntityAccessor accessor = (LivingEntityAccessor) living;
			int headLerpSteps = accessor.getLerpHeadSteps();
			if (headLerpSteps > 0) {
				// why is this a double??
				float target = (float) accessor.getLerpYHeadRot();
				float newTarget = this.apply(target, Direction.Axis.Y);
				living.lerpHeadTo(newTarget, headLerpSteps);
			}
		}

		// teleport the current lerp targets if needed
		int lerpSteps = LerpableEntity.getLerpSteps(entity);
		if (lerpSteps > 0) {
			Vec3 currentPosTarget = new Vec3(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ());
			Rotations currentRotTarget = new Rotations(entity.lerpTargetXRot(), entity.lerpTargetYRot(), 0);

			// only set each one if there's actually a difference, otherwise we might double-transform something
			Vec3 newPosTarget = currentPosTarget.equals(newPos) ? newPos : this.applyAbsolute(currentPosTarget.add(posToCenter)).add(centerToPos);
			Rotations newRotTarget = currentRotTarget.equals(newRotations) ? newRotations : this.apply(currentRotTarget);

			entity.lerpTo(newPosTarget.x, newPosTarget.y, newPosTarget.z, newRotTarget.getY(), newRotTarget.getX(), lerpSteps);
			// some entities will modify the lerpSteps, try setting it manually
			LerpableEntity.setLerpSteps(entity, lerpSteps);
		}

		// set old values. do this last, since setting non-old values above may have set them prematurely
		Vec3 oldPosTeleported = this.applyAbsolute(oldPos.add(posToCenter)).add(centerToPos);
		Rotations rotationsO = this.apply(entity.xRotO, entity.yRotO);
		entity.setOldPosAndRot(oldPosTeleported, rotationsO.getY(), rotationsO.getX());

		// anything more specific can be done by overriding this method
		entity.applyAdditionalTransforms(this);

		// reorient velocity
		Vec3 velocity = entity.getDeltaMovement();
		Vec3 newVel = this.reorientVelocity(velocity, wasGrounded);
		entity.setDeltaMovement(newVel);

		// force a sync
		entity.hasImpulse = true;
	}

	private Vec3 reorientVelocity(Vec3 velocity, boolean wasGrounded) {
		Vec3 reoriented = this.applyRelative(velocity);

		// have a minimum exit velocity, for fun.
		// this makes entities that fall into a pair of upwards-facing portals bounce up to a minimum height.
		if (!wasGrounded && reoriented.length() < PortalTeleportHandler.MIN_OUTPUT_VELOCITY) {
			// only apply when new velocity is facing mostly upwards
			Vec3 normalized = reoriented.normalize();
			double dot = normalized.dot(UP);

			if (dot > 0.9) {
				return normalized.scale(PortalTeleportHandler.MIN_OUTPUT_VELOCITY);
			}
		}

		return reoriented;
	}

	public static Quaternionf rotate180(Quaternionfc rotation) {
		return rotation.rotateZ(Mth.DEG_TO_RAD * 180, new Quaternionf());
	}
}
