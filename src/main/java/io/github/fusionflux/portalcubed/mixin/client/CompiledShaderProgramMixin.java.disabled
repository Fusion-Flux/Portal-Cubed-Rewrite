package io.github.fusionflux.portalcubed.mixin.client;

import java.util.ArrayList;
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
import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgramConfig;

@Mixin(CompiledShaderProgram.class)
public abstract class CompiledShaderProgramMixin {
	@Shadow
	@Nullable
	public abstract Uniform getUniform(String name);

	@Unique
	private List<Uniform> clippingPlanes;

	@Unique
	@Nullable
	private Uniform disintegrationColorModifier;

	@Inject(method = "setupUniforms", at = @At("TAIL"))
	private void addClippingPlaneUniform(List<ShaderProgramConfig.Uniform> uniforms, List<ShaderProgramConfig.Sampler> samplers, CallbackInfo ci) {
		this.clippingPlanes = new ArrayList<>();
		for (ShaderProgramConfig.Uniform uniformConfig : ShaderPatcher.CLIPPING_PLANE_UNIFORMS) {
			Uniform uniform = this.getUniform(uniformConfig.name());
			if (uniform != null)
				this.clippingPlanes.add(uniform);
		}

		this.disintegrationColorModifier = this.getUniform(ShaderPatcher.DISINTEGRATION_COLOR_MODIFIER_UNIFORM.name());
	}

	@Inject(method = "setDefaultUniforms", at = @At("TAIL"))
	private void updateClippingPlaneUniform(CallbackInfo ci) {
		for (int i = 0; i < this.clippingPlanes.size(); i++) {
			this.clippingPlanes.get(i).set(ShaderPatcher.CLIPPING_PLANES[i]);
		}

		if (this.disintegrationColorModifier != null)
			this.disintegrationColorModifier.set(DisintegrationRenderer.DISINTEGRATION_COLOR_MODIFIER);
	}
}
