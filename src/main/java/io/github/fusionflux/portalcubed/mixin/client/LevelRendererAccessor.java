package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor
	Frustum getCullingFrustum();
	@Accessor
	void setCullingFrustum(Frustum frustum);

	@Accessor
	RenderBuffers getRenderBuffers();
	@Accessor
	void setRenderBuffers(RenderBuffers renderBuffers);
}
