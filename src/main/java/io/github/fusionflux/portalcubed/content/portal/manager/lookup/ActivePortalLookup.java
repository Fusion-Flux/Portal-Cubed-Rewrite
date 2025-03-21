package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.collision.CollisionManager;
import net.minecraft.world.phys.Vec3;

/**
 * Fast queries for active portals for collision and teleportation.
 */
public interface ActivePortalLookup {
	/**
	 * Raycast between two points, hitting portals in between.
	 * @return null if no portals were hit
	 */
	@Nullable
	PortalHitResult clip(Vec3 from, Vec3 to);

	/**
	 * True if there are no active portals.
	 */
	boolean isEmpty();

	CollisionManager collisionManager();
}
