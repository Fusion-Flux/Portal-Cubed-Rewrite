package io.github.fusionflux.portalcubed.mixin.client;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

@Mixin(value = DefaultShaderInterface.class, remap = false)
public class DefaultShaderInterfaceMixin {
	@Unique
	private GlUniformFloat4v[] clippingPlaneUniforms;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void bindClippingPlaneUniform(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
		this.clippingPlaneUniforms = new GlUniformFloat4v[ShaderPatcher.CLIPPING_PLANE_UNIFORMS.length];
		for (int i = 0; i < this.clippingPlaneUniforms.length; i++) {
			this.clippingPlaneUniforms[i] = context.bindUniform(ShaderPatcher.CLIPPING_PLANE_UNIFORMS[i].name(), GlUniformFloat4v::new);
		}
	}

	@Inject(method = "setupState", at = @At("TAIL"))
	private void updateClippingPlaneUniform(CallbackInfo ci) {
		for (int i = 0; i < this.clippingPlaneUniforms.length; i++) {
			Vector4f vec = ShaderPatcher.CLIPPING_PLANES[i];
			this.clippingPlaneUniforms[i].set(vec.x, vec.y, vec.z, vec.w);
		}
	}
}
