package io.github.fusionflux.portalcubed.framework.extension;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import org.jetbrains.annotations.Nullable;

public interface BakedQuadExt {
	@Nullable
	BlendMode pc$blendMode();

	void pc$setBlendMode(BlendMode mode);

	@Nullable
	String pc$textureReference();

	void pc$setTextureReference(String textureReference);
}
