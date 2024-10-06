package io.github.fusionflux.portalcubed.content.portal;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	public static final double MIN_OUTPUT_VELOCITY = 0.25;

	/**
	 * Called by mixins when an entity moves relatively.
	 * Responsible for finding and teleporting through portals.
	 */
	public static boolean handle(Entity entity, double x, double y, double z) {
		Level level = entity.level();
		if (entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST) || entity instanceof Player)
			return false;

		Vec3 oldPos = entity.position();
		Vec3 oldCenter = centerOf(entity);
		Vec3 posToCenter = oldPos.vectorTo(oldCenter);
		Vec3 centerToPos = oldCenter.vectorTo(oldPos);
		Vec3 newPos = new Vec3(x, y, z);
		Vec3 newCenter = newPos.add(posToCenter);

		PortalManager manager = level.portalManager();
		PortalHitResult result = manager.activePortals().clip(oldCenter, newCenter);
		if (result == null)
			return false;

		theHorrors(result);

		boolean wasGrounded = entity.onGround(); // grab this before teleporting

		// teleport
		System.out.println("teleporting " + entity + " on " + (entity.level().isClientSide ? "client" : "server"));
		Vec3 finalCenter = result.findEnd();
		Vec3 finalPos = finalCenter.add(centerToPos);
		entity.setPos(finalPos);
		// rotate
		Rotations rotations = result.teleportRotations(entity.getXRot(), entity.getYRot());
		entity.setXRot(rotations.getX());
		entity.setYRot(rotations.getY());

		// entities will lerp to their new position normally. With portals, this results in them being sent flying to the other portal.
		// to avoid this, the old pos and rot need to be transformed as well to appear as if the entity has not been teleported.
		Vec3 oldPosTeleported = result.teleportAbsoluteVec(oldPos);
		// why are there two sets of fields that do exactly the same thing
		entity.xOld = entity.xo = oldPosTeleported.x;
		entity.yOld = entity.yo = oldPosTeleported.y;
		entity.zOld = entity.zo = oldPosTeleported.z;
		Rotations rotationsO = result.teleportRotations(entity.xRotO, entity.yRotO);
		entity.xRotO = rotationsO.getX();
		entity.yRotO = rotationsO.getY();

		reorientVelocity(entity, result, wasGrounded);

		// tp command does this
		if (entity instanceof PathfinderMob pathfinderMob) {
			pathfinderMob.getNavigation().stop();
		}

		return true;
	}

	private static Vec3 centerOf(Entity entity) {
		return entity.getBoundingBox().getCenter();
	}

	private static void theHorrors(PortalHitResult result) {
		if (result.getLast() == PortalHitResult.OVERFLOW_MARKER) {
			// skip to last 10, toString can overflow
			while (result.hasNext()) {
				result = result.next();
				if (result.depth() == 10) {
					System.out.println(result);
					break;
				}
			}
		}
	}

	private static void reorientVelocity(Entity entity, PortalHitResult result, boolean wasGrounded) {
		Vec3 vel = entity.getDeltaMovement();
		Vec3 newVel = result.teleportRelativeVec(vel);
		// have a minimum exit velocity, for fun
		// only apply when falling
		if (!wasGrounded && vel.y < 0 && newVel.length() < MIN_OUTPUT_VELOCITY) {
			newVel = newVel.normalize().scale(MIN_OUTPUT_VELOCITY);
		}
		entity.setDeltaMovement(newVel);
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

	public static Rotations teleportRotations(float xRot, float yRot, PortalInstance in, PortalInstance out) {
		Quaternionf rot = new Quaternionf()
				.rotationYXZ((180 - yRot) * Mth.DEG_TO_RAD, -xRot * Mth.DEG_TO_RAD, 0)
				.premul(in.rotation().invert(new Quaternionf()))
				.premul(out.rotation180)
				.conjugate();
		float pitch = (float) Math.atan2((rot.x * rot.w + rot.y * rot.z) * 2, 1 - 2 * (rot.x * rot.x + rot.z * rot.z));
		float yaw = (float) Math.atan2(-(rot.x * rot.z + rot.y * rot.w) * 2, 2 * (rot.y * rot.y + rot.z * rot.z) - 1);
		return new Rotations(pitch * Mth.RAD_TO_DEG, yaw * Mth.RAD_TO_DEG, 0);
	}
}
