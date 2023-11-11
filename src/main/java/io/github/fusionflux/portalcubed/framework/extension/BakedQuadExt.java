package io.github.fusionflux.portalcubed.framework.extension;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;

public interface BakedQuadExt {
	@Nullable
	RenderMaterial pc$renderMaterial();

	void pc$setRenderMaterial(RenderMaterial material);
}
