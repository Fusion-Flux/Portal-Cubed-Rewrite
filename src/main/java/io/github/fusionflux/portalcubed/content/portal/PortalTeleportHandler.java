package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	public static final double MIN_OUTPUT_VELOCITY = 0.1;

	/**
	 * Called by mixins when an entity moves relatively.
	 * Responsible for finding and teleporting through portals.
	 */
	public static void handle(Entity entity, double x, double y, double z, PositionSetter setter) {
		Level level = entity.level();
		if (level.isClientSide || entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST)) {
			setter.set(entity, x, y, z);
			return;
		}

		Vec3 oldPos = entity.position();
		Vec3 newPos = new Vec3(x, y, z);
		PortalManager manager = level.portalManager();
		PortalHitResult result = manager.activePortals().clip(oldPos, newPos);
		if (result == null) {
			setter.set(entity, x, y, z);
			return;
		}

		boolean wasGrounded = entity.onGround(); // grab this before teleporting

		Vec3 oldPosTeleported = result.teleportAbsoluteVec(oldPos);
		Vec3 newPosTeleported = result.findEnd();
		// todo: avoid player stats going haywire
		teleportNoLerp(entity, newPosTeleported);

		// rotate entity
		Vec3 lookVec = result.teleportRelativeVec(entity.getLookAngle());
		Vec3 lookTarget = entity.getEyePosition().add(lookVec);
		entity.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);

		// reorient velocity
		Vec3 vel = entity.getDeltaMovement();
		Vec3 newVel = result.teleportRelativeVec(vel);
		// have a minimum exit velocity, for fun
		// only apply when falling
		if (!wasGrounded && vel.y < 0 && newVel.length() < MIN_OUTPUT_VELOCITY) {
			newVel = newVel.normalize().scale(MIN_OUTPUT_VELOCITY);
		}
		entity.setDeltaMovement(newVel);

		// tp command does this
		if (entity instanceof PathfinderMob pathfinderMob) {
			pathfinderMob.getNavigation().stop();
		}
	}

	public static void teleportNoLerp(Entity entity, Vec3 pos) {
		entity.teleportTo(pos.x, pos.y, pos.z);
	}

	public static Vec3 teleportAbsoluteVecBetween(Vec3 vec, PortalInstance in, PortalInstance out) {
		return TransformUtils.apply(
				vec,
				in::relativize,
				in.rotation::transformInverse,
				out.rotation180::transform,
				out::derelativize
		);
	}

	public static Vec3 teleportRelativeVecBetween(Vec3 vec, PortalInstance in, PortalInstance out) {
		return TransformUtils.apply(
				vec,
				in.rotation::transformInverse,
				out.rotation180::transform
		);
	}

	public interface PositionSetter {
		void set(Entity entity, double x, double y, double z);
	}
}
