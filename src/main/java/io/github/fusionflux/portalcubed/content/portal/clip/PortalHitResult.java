package io.github.fusionflux.portalcubed.content.portal.clip;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.ref.HitPortal;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;

/**
 * Represents the result of a raycast that only interacts with portals.
 * <p>
 * A PortalHitResult has two components: a path, and a final portal that was hit. Both component are optional.
 * @param path the path through open portals the raycast followed
 * @param finalPortal a final closed portal that the raycast hit
 */
public record PortalHitResult(Optional<PortalPath> path, Optional<HitPortal> finalPortal) {
	public static final PortalHitResult EMPTY = new PortalHitResult(Optional.empty(), Optional.empty());

	public PortalHitResult(List<PortalPath.Entry> entries, @Nullable HitPortal finalPortal) {
		this(PortalPath.ofOptional(entries), Optional.ofNullable(finalPortal));
	}

	public boolean isEmpty() {
		return this.path.isEmpty() && this.finalPortal.isEmpty();
	}
}
