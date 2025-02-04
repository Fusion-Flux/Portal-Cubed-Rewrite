package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

public interface BakedQuadExt {
	@Nullable
	default BlendMode pc$blendMode() {
		throw new AbstractMethodError();
	}

	default void pc$setBlendMode(BlendMode mode) {
		throw new AbstractMethodError();
	}

	@Nullable
	default String pc$textureReference() {
		throw new AbstractMethodError();
	}

	default void pc$setTextureReference(String textureReference) {
		throw new AbstractMethodError();
	}
}
