package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

public interface BlockElementExt {
	@Nullable
	default String pc$name() {
		throw new AbstractMethodError();
	}

	default void pc$setName(String name) {
		throw new AbstractMethodError();
	}


	@Nullable
	default BlendMode pc$blendMode() {
		throw new AbstractMethodError();
	}

	default void pc$setBlendMode(BlendMode mode) {
		throw new AbstractMethodError();
	}
}
