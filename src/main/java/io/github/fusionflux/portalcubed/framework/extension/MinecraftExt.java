package io.github.fusionflux.portalcubed.framework.extension;

import org.jspecify.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;

public interface MinecraftExt {
	default RaycastResult.@Nullable Portal selectedPortal() {
		throw new AbstractMethodError();
	}

	default void setSelectedPortal(RaycastResult.@Nullable Portal result) {
		throw new AbstractMethodError();
	}
}
