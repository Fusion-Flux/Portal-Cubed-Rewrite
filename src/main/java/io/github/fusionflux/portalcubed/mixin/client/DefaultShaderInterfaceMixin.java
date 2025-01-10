package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

@Mixin(value = DefaultShaderInterface.class, remap = false)
public class DefaultShaderInterfaceMixin {
	@Unique
	private GlUniformFloat4v uniformClippingPlane;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void bindClippingPlaneUniform(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
		this.uniformClippingPlane = context.bindUniform(ShaderPatcher.CLIPPING_PLANE_UNIFORM_NAME, GlUniformFloat4v::new);
	}

	@Inject(method = "setupState", at = @At("TAIL"))
	private void updateClippingPlaneUniform(CallbackInfo ci) {
		if (PortalRenderer.isRenderingView()) {
			Vector4f clippingPlane = PortalRenderer.CLIPPING_PLANES.get();
			this.uniformClippingPlane.set(new float[]{clippingPlane.x, clippingPlane.y, clippingPlane.z, clippingPlane.w});
		}
	}
}
