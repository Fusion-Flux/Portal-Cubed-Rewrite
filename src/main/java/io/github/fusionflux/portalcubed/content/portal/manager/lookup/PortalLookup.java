package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Interface for various spatial portal lookups.
 */
public interface PortalLookup {
	/**
	 * Perform a raycast with no recursion limit.
	 * @see #clip(Vec3, Vec3, int)
	 */
	@Nullable
	default PortalHitResult clip(Vec3 from, Vec3 to) {
		return this.clip(from, to, Integer.MAX_VALUE);
	}

	/**
	 * Raycast between two points, hitting portals in between.
	 * @param maxDepth the maximum number of portal pairs to pass through before giving up
	 * @return null if no portals were hit
	 */
	@Nullable
	PortalHitResult clip(Vec3 from, Vec3 to, int maxDepth);

	/**
	 * Perform a raycast with a maximum depth of 1. Will always be a {@link PortalHitResult.Tail} if a hit occurs.
	 */
	@Nullable
	default PortalHitResult.Tail clipOnce(Vec3 from, Vec3 to) {
		PortalHitResult result = this.clip(from, to, 1);

		if (result == null) {
			return null;
		} else if (result instanceof PortalHitResult.Tail tail) {
			return tail;
		} else {
			throw new IllegalStateException("clip(from, to, 1) should always return a PortalHitResult.Tail");
		}
	}

	/**
	 * Find all portals intersecting the given box.
	 * @return a new, mutable set containing any found portals
	 */
	@Contract("_->new")
	Set<PortalReference> getPortals(AABB bounds);

	/**
	 * Find all portals intersecting the given sphere.
	 * @return a new, mutable set containing any found portals
	 */
	@Contract("_,_->new")
	Set<PortalReference> getPortalsAround(Vec3 pos, double radius);

	boolean isEmpty();
}
