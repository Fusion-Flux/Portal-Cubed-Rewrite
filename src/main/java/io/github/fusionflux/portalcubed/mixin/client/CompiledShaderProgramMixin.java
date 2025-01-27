package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.shaders.Uniform;

import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationRenderer;
import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgramConfig;

@Mixin(CompiledShaderProgram.class)
public abstract class CompiledShaderProgramMixin {
	@Shadow
	@Nullable
	public abstract Uniform getUniform(String name);

	@Unique
	@Nullable
	private Uniform clippingPlane;

	@Unique
	@Nullable
	private Uniform disintegrationColorModifier;

	@Inject(method = "setupUniforms", at = @At("TAIL"))
	private void addClippingPlaneUniform(List<ShaderProgramConfig.Uniform> uniforms, List<ShaderProgramConfig.Sampler> samplers, CallbackInfo ci) {
		this.clippingPlane = this.getUniform(ShaderPatcher.CLIPPING_PLANE_UNIFORM_NAME);
		this.disintegrationColorModifier = this.getUniform(ShaderPatcher.DISINTEGRATION_COLOR_MODIFIER_UNIFORM.name());
	}

	@Inject(
			method = "apply",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/GlStateManager;_activeTexture(I)V",
					shift = At.Shift.AFTER,
					remap = false
			)
	)
	private void updateClippingPlaneUniform(CallbackInfo ci) {
		if (this.clippingPlane != null && PortalRenderer.isRenderingView())
			this.clippingPlane.set(PortalRenderer.CLIPPING_PLANES.get());
		if (this.disintegrationColorModifier != null)
			this.disintegrationColorModifier.set(DisintegrationRenderer.DISINTEGRATION_COLOR_MODIFIER);
	}
}
