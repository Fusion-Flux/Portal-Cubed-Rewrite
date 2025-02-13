package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.sync.TeleportProgressTracker;

public interface PortalTeleportationExt {
	default int pc$getPortalCollisionRecursionDepth() {
		throw new AbstractMethodError();
	}

	default void pc$setPortalCollisionRecursionDepth(int depth) {
		throw new AbstractMethodError();
	}

	default void pc$setNextTeleportNonLocal(boolean value) {
		throw new AbstractMethodError();
	}

	default boolean pc$isNextTeleportNonLocal() {
		throw new AbstractMethodError();
	}

	// no prefix needed, unique descriptors

	default TeleportProgressTracker getTeleportProgressTracker() {
		throw new AbstractMethodError();
	}
}
