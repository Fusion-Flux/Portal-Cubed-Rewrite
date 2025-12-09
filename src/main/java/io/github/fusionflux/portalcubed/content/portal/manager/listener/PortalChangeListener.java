package io.github.fusionflux.portalcubed.content.portal.manager.listener;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;

public interface PortalChangeListener {
	/**
	 * Invoked after the given portal has been created.
	 */
	default void portalCreated(PortalReference reference) {
	}

	/**
	 * Invoked after the portal has been modified.
	 * @param reference the reference, which now holds the updated portal
	 */
	default void portalModified(Portal oldPortal, PortalReference reference) {
	}

	/**
	 * Invoked after the given portal has been removed.
	 * @param reference the now-removed reference
	 * @param portal the portal that was removed
	 */
	default void portalRemoved(PortalReference reference, Portal portal) {
	}
}
