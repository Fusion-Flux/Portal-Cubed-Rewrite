package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;

public interface MinecraftExt {
	@Nullable
	default RaycastResult.Portal selectedPortal() {
		throw new AbstractMethodError();
	}

	default void setSelectedPortal(@Nullable RaycastResult.Portal result) {
		throw new AbstractMethodError();
	}
}
