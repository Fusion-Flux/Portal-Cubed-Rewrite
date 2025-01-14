package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;

public interface PlayerExt {
	// note: no prefixes needed, descriptors guaranteed unique by HoldableEntity
	default void setHeldEntity(HoldableEntity heldEntity) {
		throw new AbstractMethodError();
	}

	@Nullable
	default HoldableEntity getHeldEntity() {
		throw new AbstractMethodError();
	}

	default void pc$setHasSubmergedTheOperationalEndOfTheDevice(boolean hasSubmergedTheOperationalEndOfTheDevice) {
		throw new AbstractMethodError();
	}

	default boolean pc$hasSubmergedTheOperationalEndOfTheDevice() {
		throw new AbstractMethodError();
	}
}
