package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.framework.util.RangeSequence;

import org.joml.Quaternionf;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.Rotations;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	public static final double MIN_OUTPUT_VELOCITY = 0.25;

	/**
	 * Called by mixins when an entity moves relatively.
	 * Responsible for finding and teleporting through portals.
	 */
	public static boolean handle(Entity entity, double x, double y, double z) {
		if (isTeleportBlocked(entity))
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
		System.out.println("teleporting " + entity + " on " + (entity.level().isClientSide ? "client" : "server"));
		Vec3 finalCenter = result.findEnd();
		Vec3 finalPos = finalCenter.add(centerToPos);
		entity.setPos(finalPos);
		// rotate
		Rotations rotations = new Rotations(entity.getXRot(), entity.getYRot(), 0);
		Rotations newRotations = result.teleportRotations(rotations);
		entity.setXRot(newRotations.getX());
		entity.setYRot(newRotations.getY());

		// entities will lerp to their new position normally. With portals, this results in them being sent flying to the other portal.
		// to avoid this, the old pos and rot need to be transformed as well to appear as if the entity has not been teleported.
		Vec3 oldPosTeleported = result.teleportAbsoluteVec(oldPos);
		// why are there two sets of fields that do exactly the same thing
		entity.xOld = entity.xo = oldPosTeleported.x;
		entity.yOld = entity.yo = oldPosTeleported.y;
		entity.zOld = entity.zo = oldPosTeleported.z;
		Rotations rotationsO = result.teleportRotations(entity.xRotO, entity.yRotO, 0);
		entity.xRotO = rotationsO.getX();
		entity.yRotO = rotationsO.getY();

		Vec3 initialVel = entity.getDeltaMovement();
		Vec3 newVel = reorientVelocity(entity, result, wasGrounded);
		entity.setDeltaMovement(newVel);

		// tp command does this
		if (entity instanceof PathfinderMob pathfinderMob) {
			pathfinderMob.getNavigation().stop();
		}

		// sync to clients
		double distance = oldPos.distanceTo(newPos);

		RangeSequence<TeleportStep> steps = buildTeleportSteps(result, distance, initialVel, newVel, rotations);
		// store the steps on the entity for ServerEntityMixin.
		// this being non-null causes it to be synced.
		entity.setPortalTeleport(steps);

		return true;
	}

	private static Vec3 centerOf(Entity entity) {
		return entity.getBoundingBox().getCenter();
	}

	public static Vec3 getCenterToPosOffset(Entity entity) {
		return centerOf(entity).vectorTo(entity.position());
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

	private static Vec3 reorientVelocity(Entity entity, PortalHitResult result, boolean wasGrounded) {
		Vec3 vel = entity.getDeltaMovement();
		Vec3 newVel = result.teleportRelativeVec(vel);
		// have a minimum exit velocity, for fun
		// only apply when falling
		if (!wasGrounded && vel.y < 0 && newVel.length() < MIN_OUTPUT_VELOCITY) {
			newVel = newVel.normalize().scale(MIN_OUTPUT_VELOCITY);
		}
		return newVel;
	}

	private static RangeSequence<TeleportStep> buildTeleportSteps(PortalHitResult result, double totalDistance,
																  Vec3 vel, Vec3 endVel, Rotations rotations) {
		RangeSequence.Builder<TeleportStep> steps = RangeSequence.start(0);
		// single result: 2 lerpable states, 4 states (start, enter, exit, end)
		// 2 results: 3 lerpable states, 6 states (start, enter1, exit1, enter2, exit2, end)
		// 3 results: 4 lerpable states, 8 states (start, enter1, exit1, enter2, exit2, enter3, exit3, end)
		// add start -> enter1
		double distance = result.start().distanceTo(result.inHit());
		double progress = distance / totalDistance;

		steps.until(progress, new TeleportStep(result.start(), result.inHit(), vel, rotations));

		// add intermediate steps, exitN -> enterN + 1
		while (result.hasNext()) {
			PortalHitResult next = result.next();

			distance = result.outHit().distanceTo(next.inHit());
			progress = progress + (distance / totalDistance);
			vel = result.teleportRelativeVec(vel);
			rotations = result.teleportRotations(rotations);

			steps.until(progress, new TeleportStep(
					result.outHit(),
					next.inHit(),
					vel, rotations
			));

			result = next;
		}

		// result is now the final one, add exitN -> end
		rotations = result.teleportRotations(rotations);
		// use endVel here because it may be different (min exit velocity)
		steps.until(1, new TeleportStep(result.outHit(), result.end(), endVel, rotations));
		return steps.build();
	}

	public static boolean isTeleportBlocked(Entity entity) {
		return entity instanceof Player
				|| entity.level().isClientSide
				|| entity.isPassenger()
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
}
