package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.TeleportProgressTracker;

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

	@Nullable
	default TeleportProgressTracker getTeleportProgressTracker() {
		throw new AbstractMethodError();
	}

	default void setTeleportProgressTracker(@Nullable TeleportProgressTracker tracker) {
		throw new AbstractMethodError();
	}
}
