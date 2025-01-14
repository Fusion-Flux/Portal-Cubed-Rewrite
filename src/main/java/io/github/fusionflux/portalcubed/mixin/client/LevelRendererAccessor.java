package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;

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

	@Invoker
	void callPrepareCullFrustum(Vec3 cameraPosition, Matrix4f frustumMatrix, Matrix4f projectionMatrix);
}
