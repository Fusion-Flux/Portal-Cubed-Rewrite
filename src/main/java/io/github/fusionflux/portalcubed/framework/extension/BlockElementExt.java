package io.github.fusionflux.portalcubed.framework.extension;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

import org.jetbrains.annotations.Nullable;

public interface BlockElementExt {
	@Nullable
	String pc$name();

	void pc$setName(String name);


	@Nullable
	BlendMode pc$blendMode();

	void pc$setBlendMode(BlendMode mode);
}
