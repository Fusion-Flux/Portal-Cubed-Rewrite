package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.CompiledShader;

import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.minecraft.resources.ResourceLocation;

@Mixin(CompiledShader.class)
public class CompiledShaderMixin {
	@ModifyArg(method = "compile", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glShaderSource(ILjava/lang/String;)V", remap = false))
	private static String patchShaderSource(String source, @Local(argsOnly = true) ResourceLocation name, @Local(argsOnly = true) CompiledShader.Type type) {
		return ShaderPatcher.tryPatch(source, name + (type == CompiledShader.Type.VERTEX ? ".vsh" : ".fsh")).orElse(source);
	}
}
