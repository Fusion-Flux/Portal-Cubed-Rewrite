package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Invoker
	void callPrepareCullFrustum(PoseStack matrices, Vec3 pos, Matrix4f projectionMatrix);

	@Accessor
	Frustum getCullingFrustum();
	@Accessor
	void setCullingFrustum(Frustum frustum);

	@Accessor
	PostChain getEntityEffect();
	@Accessor
	void setEntityEffect(PostChain postChain);

	@Accessor
	RenderBuffers getRenderBuffers();
	@Accessor
	void setRenderBuffers(RenderBuffers renderBuffers);
}
