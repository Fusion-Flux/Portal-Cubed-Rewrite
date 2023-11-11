package io.github.fusionflux.portalcubed.content.portal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalTeleportHandler {
	/**
	 * Called by mixins when an entity moves relatively.
	 * Responsible for finding and teleporting through portals.
	 */
	public static void handle(Entity entity, double x, double y, double z, Operation<Void> setPos) {
		Level level = entity.level();
		if (level.isClientSide || entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST)) {
			setPos.call(entity, x, y, z);
			return;
		}

		Vec3 oldPos = entity.position();
		Vec3 newPos = new Vec3(x, y, z);
		PortalManager manager = PortalManager.of(level);
		PortalHitResult result = manager.clipPortal(oldPos, newPos);
		if (result != null) {
			Vec3 teleported = result.teleportedEnd();
			entity.teleportTo(teleported.x, teleported.y, teleported.z);
			// rotate entity
			Vec3 lookVec = TransformUtils.apply(
					entity.getLookAngle(),
					result.in().rotation::transformInverse,
					result.out().rotation180::transform
			);
			Vec3 lookTarget = entity.getEyePosition().add(lookVec);
			entity.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);
			// TODO: should we teleport the old position fields to behind the out portal?
		} else {
			setPos.call(entity, x, y, z);
		}
	}
}
