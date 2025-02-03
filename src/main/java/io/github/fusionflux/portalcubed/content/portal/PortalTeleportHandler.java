package io.github.fusionflux.portalcubed.content.portal;

import org.joml.Quaternionf;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.entity.LerpableEntity;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.EntityAccessor;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.PortalTeleportPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ClientTeleportedPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	public static final double MIN_OUTPUT_VELOCITY = 0.5;
	public static final double DISTANCE_TO_STEP_BACK = new Vec3(
			1f / VoxelShenanigans.OBB_APPROXIMATION_RESOLUTION,
			1f / VoxelShenanigans.OBB_APPROXIMATION_RESOLUTION,
			1f / VoxelShenanigans.OBB_APPROXIMATION_RESOLUTION
	).length();

	/**
	 * Called by mixins when an entity moves relatively.
	 * Responsible for finding and teleporting through portals.
	 */
	public static boolean handle(Entity entity, double x, double y, double z) {
		if (isTeleportBlocked(entity) || entity instanceof ServerPlayer)
			return false;

		Vec3 oldPos = entity.position();
		Vec3 oldCenter = centerOf(entity);
		Vec3 posToCenter = oldPos.vectorTo(oldCenter);
		Vec3 centerToPos = oldCenter.vectorTo(oldPos);
		Vec3 newPos = new Vec3(x, y, z);
		Vec3 newCenter = newPos.add(posToCenter);

		PortalManager manager = entity.level().portalManager();
		PortalHitResult result = manager.activePortals().clip(oldCenter, newCenter);
		if (result == null)
			return false;

		if (theHorrors(result))
			return false;

		boolean wasGrounded = entity.onGround(); // grab this before teleporting

		// teleport
		Vec3 finalCenter = result.findEnd();
		Vec3 finalPos = finalCenter.add(centerToPos);
		entity.setPos(finalPos);
		nudge(entity, result.getLast().out());
		// old pos
		Vec3 oldPosTeleported = result.teleportAbsoluteVec(oldCenter).add(centerToPos);
		// why are there two sets of fields that do exactly the same thing
		entity.xOld = entity.xo = oldPosTeleported.x;
		entity.yOld = entity.yo = oldPosTeleported.y;
		entity.zOld = entity.zo = oldPosTeleported.z;

		// rotate
		// head and body first
		if (entity instanceof LivingEntity living) {
//			living.setYHeadRot(result.teleportRotation(living.yHeadRot, Direction.Axis.Y));
//			living.yHeadRotO = result.teleportRotation(living.yHeadRotO, Direction.Axis.Y);
//			living.setYBodyRot(result.teleportRotation(living.yBodyRot, Direction.Axis.Y));
//			living.yBodyRotO = result.teleportRotation(living.yBodyRotO, Direction.Axis.Y);
		}

		Rotations newRotations = result.teleportRotations(entity.getXRot(), entity.getYRot(), 0);
		entity.setXRot(newRotations.getWrappedX());
		entity.setYRot(newRotations.getWrappedY());

		Rotations rotationsO = result.teleportRotations(entity.xRotO, entity.yRotO, 0);
		entity.xRotO = rotationsO.getWrappedX();
		entity.yRotO = rotationsO.getWrappedY();

		// teleport the current lerp target
		Vec3 oldTarget = new Vec3(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ());
		if (!oldTarget.equals(oldPos)) {
			Vec3 oldTargetCentered = oldTarget.add(posToCenter);
			Rotations oldLerpRotations = new Rotations(entity.lerpTargetXRot(), entity.lerpTargetYRot(), 0);
			Vec3 newTargetCentered = result.teleportAbsoluteVec(oldTargetCentered);
			Vec3 newTarget = newTargetCentered.subtract(posToCenter);
			Rotations newLerpRotations = result.teleportRotations(oldLerpRotations);
			int lerpSteps = LerpableEntity.getLerpSteps(entity);
			entity.lerpTo(newTarget.x, newTarget.y, newTarget.z, newLerpRotations.getWrappedY(), newLerpRotations.getWrappedX(), 0);
			LerpableEntity.setLerpSteps(entity, lerpSteps);
		}

		// reorient velocity
		Vec3 newVel = reorientVelocity(entity, result, wasGrounded);
		entity.setDeltaMovement(newVel);
		entity.hasImpulse = true;

		// tp command does this
		if (entity instanceof PathfinderMob pathfinderMob) {
			pathfinderMob.getNavigation().stop();
		}

		// syncing stuff
		if (entity instanceof Player player && player.isLocalPlayer()) {
			// players are handled specially. All the logic is client side and the server is notified.
			// server does some verification and tells the client if the teleport was invalid.
			PortalTeleportInfo info = buildTeleportInfo(result);
			ClientTeleportedPacket packet = new ClientTeleportedPacket(info, entity.position(), entity.getXRot(), entity.getYRot());
			PortalCubedPackets.sendToServer(packet);
			return true;
		}

		if (entity.level().isClientSide) {
			// update tracker if present
			updateProgressTracker(entity, result);
		} else {
			// sync to clients
			PortalTeleportInfo info = buildTeleportInfo(result);
			PortalTeleportPacket packet = new PortalTeleportPacket(entity.getId(), info);
			PortalCubedPackets.sendToClients(PlayerLookup.tracking(entity), packet);
		}

		return true;
	}

	public static Vec3 centerOf(Entity entity) {
		return entity.getBoundingBox().getCenter();
	}

	private static boolean theHorrors(PortalHitResult result) {
		if (result.getLast() == PortalHitResult.OVERFLOW_MARKER) {
			// skip to last 10, toString can overflow
			while (result.hasNext()) {
				result = result.next();
				if (result.depth() == 10) {
					System.out.println(result);
				}
			}
			return true;
		}
		return false;
	}

	private static void nudge(Entity entity, PortalInstance exited) {
		// because of the difference in a portal's plane and its collision, teleporting will always put an entity
		// either in the ground or floating slightly, depending on direction. Need to nudge the entity towards the
		// center and then back to the intended pos, stopping early if collision is hit.
		Vec3 center = centerOf(entity);
		Vec3 stepBack = center.vectorTo(exited.data.origin()).normalize().scale(DISTANCE_TO_STEP_BACK);
		entity.setPos(entity.position().add(stepBack));
		Vec3 stepForwards = stepBack.scale(-1);
		Vec3 completedStep = ((EntityAccessor) entity).callCollide(stepForwards);
		entity.setPos(entity.position().add(completedStep));
	}

	private static Vec3 reorientVelocity(Entity entity, PortalHitResult result, boolean wasGrounded) {
		Vec3 vel = entity.getDeltaMovement();
		Vec3 newVel = result.teleportRelativeVec(vel);
		// have a minimum exit velocity, for fun
		// only apply when new vel is facing upwards
		if (!wasGrounded && newVel.y > 0 && newVel.length() < MIN_OUTPUT_VELOCITY) {
			newVel = newVel.normalize().scale(MIN_OUTPUT_VELOCITY);
		}
		return newVel;
	}

	private static void updateProgressTracker(Entity entity, PortalHitResult result) {
		TeleportProgressTracker tracker = entity.getTeleportProgressTracker();
		if (tracker == null)
			return;

		while (result != null) {
			tracker.notify(result.pairKey(), result.pair().polarityOf(result.in()));
			if (tracker.isComplete()) {
				System.out.println("tracking done");
				entity.setTeleportProgressTracker(null);
				break;
			}

			result = result.nextOrNull();
		}
	}

	private static PortalTeleportInfo buildTeleportInfo(PortalHitResult result) {
		return new PortalTeleportInfo(
				result.pairKey(),
				result.pair().polarityOf(result.in()),
				result.hasNext() ? buildTeleportInfo(result.next()) : null
		);
	}

	public static boolean isTeleportBlocked(Entity entity) {
		return entity.isPassenger()
				|| !entity.getPassengers().isEmpty()
				|| entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST);
	}

	// teleportation utilities

	public static OBB teleportBox(OBB box, PortalInstance in, PortalInstance out) {
		return box.transformed(
				center -> teleportAbsoluteVecBetween(center, in, out),
				rotation -> rotation
						.rotateLocal(in.rotation())
						.rotateLocal(out.rotation180)
		);
	}

	public static Vec3 teleportAbsoluteVecBetween(Vec3 vec, PortalInstance in, PortalInstance out) {
		return TransformUtils.apply(
				vec,
				in::relativize,
				in.rotation()::transformInverse,
				out.rotation180::transform,
				out::derelativize
		);
	}

	public static Vec3 teleportRelativeVecBetween(Vec3 vec, PortalInstance in, PortalInstance out) {
		return TransformUtils.apply(
				vec,
				in.rotation()::transformInverse,
				out.rotation180::transform
		);
	}

	public static Rotations teleportRotations(float xRot, float yRot, float zRot, PortalInstance in, PortalInstance out) {
		Quaternionf rot = new Quaternionf()
				.rotationYXZ((180 - yRot) * Mth.DEG_TO_RAD, -xRot * Mth.DEG_TO_RAD, 0) // TODO add zRot
				.premul(in.rotation().invert(new Quaternionf()))
				.premul(out.rotation180)
				.conjugate();
		float pitch = (float) Math.atan2((rot.x * rot.w + rot.y * rot.z) * 2, 1 - 2 * (rot.x * rot.x + rot.z * rot.z));
		float yaw = (float) Math.atan2(-(rot.x * rot.z + rot.y * rot.w) * 2, 2 * (rot.y * rot.y + rot.z * rot.z) - 1);
		return new Rotations(pitch * Mth.RAD_TO_DEG, yaw * Mth.RAD_TO_DEG, 0);
	}

	/**
	 * Returns wrapped degrees
	 */
	public static float teleportRotation(float degrees, Direction.Axis axis, PortalInstance in, PortalInstance out) {
		Quaternionf rot = new Quaternionf();
		switch (axis) {
			case X -> rot.rotationX((180 - degrees) * Mth.DEG_TO_RAD);
			case Y -> rot.rotationY(-degrees * Mth.DEG_TO_RAD);
			case Z -> throw new RuntimeException();
		}

		rot.premul(in.rotation().invert(new Quaternionf()))
				.premul(out.rotation180)
				.conjugate();

		return (float) Mth.wrapDegrees(Mth.RAD_TO_DEG * switch (axis) {
			case X -> Math.atan2((rot.x * rot.w + rot.y * rot.z) * 2, 1 - 2 * (rot.x * rot.x + rot.z * rot.z));
			case Y -> Math.atan2(-(rot.x * rot.z + rot.y * rot.w) * 2, 2 * (rot.y * rot.y + rot.z * rot.z) - 1);
			case Z -> throw new RuntimeException();
		});
	}
}
