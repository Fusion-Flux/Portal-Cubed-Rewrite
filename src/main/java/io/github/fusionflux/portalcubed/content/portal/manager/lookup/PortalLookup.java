package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Contract;

import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.ref.HitPortal;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Interface for various spatial portal lookups.
 */
public interface PortalLookup {
	int DEFAULT_RECURSION_LIMIT = 64;

	/**
	 * @return true if this lookup contains no portals
	 */
	boolean isEmpty();

	/**
	 * Perform a raycast with a reasonable default recursion limit, ignoring closed portals.
	 * @see #clip(Vec3, Vec3, int)
	 */
	default PortalHitResult clip(Vec3 from, Vec3 to) {
		return this.clip(from, to, DEFAULT_RECURSION_LIMIT);
	}

	/**
	 * Perform a raycast that ignores closed portals.
	 * @see #clip(Vec3, Vec3, int, boolean)
	 */
	default PortalHitResult clip(Vec3 from, Vec3 to, int recursionLimit) {
		return this.clip(from, to, recursionLimit, false);
	}

	/**
	 * Raycast between two points, hitting portals in between.
	 * @param recursionLimit the maximum number of portal pairs to pass through before giving up
	 * @param hitClosed if true, closed portals may be hit, ending the raycast early
	 * @return a {@link PortalHitResult}. If {@code allowClosed} is false, the result's closed portal should always be empty.
	 */
	PortalHitResult clip(Vec3 from, Vec3 to, int recursionLimit, boolean hitClosed);

	/**
	 * Perform a raycast without recursing, ignoring closed portals.
	 */
	default Optional<HitPortal> clipOnce(Vec3 from, Vec3 to) {
		return this.clipOnce(from, to, false);
	}

	/**
	 * Perform a raycast without recursing.
	 * @param hitClosed if true, closed portals may be hit, ending the raycast early
	 */
	default Optional<HitPortal> clipOnce(Vec3 from, Vec3 to, boolean hitClosed) {
		PortalHitResult result = this.clip(from, to, 0, hitClosed);
		if (result.path().isPresent()) {
			throw new IllegalStateException("Recursion limit is 0, no portals should've been passed through");
		}

		return result.finalPortal();
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
}
