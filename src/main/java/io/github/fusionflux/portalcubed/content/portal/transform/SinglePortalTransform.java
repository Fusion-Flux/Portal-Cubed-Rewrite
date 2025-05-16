package io.github.fusionflux.portalcubed.content.portal.transform;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.framework.entity.LerpableEntity;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Rotations;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class SinglePortalTransform implements PortalTransform {
	public static final StreamCodec<ByteBuf, SinglePortalTransform> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, transform -> transform.inOrigin,
			PortalCubedStreamCodecs.QUATERNIONFC, transform -> transform.inRot,
			Vec3.STREAM_CODEC, transform -> transform.outOrigin,
			PortalCubedStreamCodecs.QUATERNIONFC, transform -> transform.outRot,
			SinglePortalTransform::new
	);

	public final Vec3 inOrigin;
	public final Quaternionfc inRot;
	public final Quaternionfc inRot180;

	public final Vec3 outOrigin;
	public final Quaternionfc outRot;
	public final Quaternionfc outRot180;

	public final PortalTransform inverse;

	public SinglePortalTransform(PortalHitResult.Open hitResult) {
		this(hitResult.enteredPortal().portal(), hitResult.exitedPortal().portal());
	}

	public SinglePortalTransform(PortalInstance in, PortalInstance out) {
		this(in.data.origin(), in.data.rotation(), out.data.origin(), out.data.rotation());
	}

	public SinglePortalTransform(Vec3 inOrigin, Quaternionfc inRot, Vec3 outOrigin, Quaternionfc outRot) {
		this(inOrigin, inRot, outOrigin, outRot, null);
	}

	private SinglePortalTransform(Vec3 inOrigin, Quaternionfc inRot, Vec3 outOrigin, Quaternionfc outRot, @Nullable PortalTransform inverse) {
		this.inOrigin = inOrigin;
		this.inRot = new Quaternionf(inRot);
		this.inRot180 = rotate180(inRot);
		this.outOrigin = outOrigin;
		this.outRot = new Quaternionf(outRot);
		this.outRot180 = rotate180(outRot);
		this.inverse = inverse != null ? inverse : new SinglePortalTransform(outOrigin, outRot, inOrigin, inRot, this);
	}

	@Override
	public Vector3d applyRelative(Vector3d pos) {
		this.inRot.transformInverse(pos);
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
		return rotation
				.rotateLocal(this.inRot)
				.rotateLocal(this.outRot180);
	}

	@Override
	public Rotations apply(Rotations rotations) {
		// TODO: handle Z
		Quaternionf rot = new Quaternionf()
				.rotationYXZ((180 - rotations.getY()) * Mth.DEG_TO_RAD, -rotations.getX() * Mth.DEG_TO_RAD, 0)
				.premul(this.inRot.invert(new Quaternionf()))
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

		boolean wasGrounded = entity.onGround(); // grab this before teleporting

		// teleport
		entity.setPos(this.applyAbsolute(center).add(centerToPos));
		PortalTeleportHandler.nudge(entity, this.outOrigin);

		// rotate
		Rotations newRotations = this.apply(entity.getXRot(), entity.getYRot());
		entity.setXRot(newRotations.getWrappedX());
		entity.setYRot(newRotations.getWrappedY());

		// old values
		Vec3 oldPosTeleported = this.applyAbsolute(entity.oldPosition().add(posToCenter)).add(centerToPos);
		Rotations rotationsO = this.apply(entity.xRotO, entity.yRotO);
		entity.setOldPosAndRot(oldPosTeleported, rotationsO.getWrappedY(), rotationsO.getWrappedX());

		if (entity instanceof LivingEntity living) {
//			living.setYHeadRot(result.teleportRotation(living.yHeadRot, Direction.Axis.Y));
//			living.yHeadRotO = result.teleportRotation(living.yHeadRotO, Direction.Axis.Y);
//			living.setYBodyRot(result.teleportRotation(living.yBodyRot, Direction.Axis.Y));
//			living.yBodyRotO = result.teleportRotation(living.yBodyRotO, Direction.Axis.Y);
		}

		// teleport the current lerp target
		Vec3 oldTarget = new Vec3(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ());
		// target is current pos when no lerp, only teleport if it's different
		if (!oldTarget.equals(entity.position())) {
			Vec3 newTarget = this.applyAbsolute(oldTarget.add(posToCenter)).add(centerToPos);
			Rotations newLerpRotations = this.apply(entity.lerpTargetXRot(), entity.lerpTargetYRot());
			int lerpSteps = LerpableEntity.getLerpSteps(entity);
			entity.lerpTo(newTarget.x, newTarget.y, newTarget.z, newLerpRotations.getWrappedY(), newLerpRotations.getWrappedX(), lerpSteps);
			// some entities will modify the lerpSteps, try setting it manually
			LerpableEntity.setLerpSteps(entity, lerpSteps);
		}

		// reorient velocity
		Vec3 newVel = this.applyRelative(entity.getDeltaMovement());
		// have a minimum exit velocity, for fun
		// only apply when new vel is facing upwards
		if (!wasGrounded && newVel.y > 0 && newVel.length() < PortalTeleportHandler.MIN_OUTPUT_VELOCITY) {
			newVel = newVel.normalize().scale(PortalTeleportHandler.MIN_OUTPUT_VELOCITY);
		}
		entity.setDeltaMovement(newVel);
		entity.hasImpulse = true;
	}

	public static Quaternionf rotate180(Quaternionfc rotation) {
		return rotation.rotateZ(Mth.DEG_TO_RAD * 180, new Quaternionf());
	}
}
