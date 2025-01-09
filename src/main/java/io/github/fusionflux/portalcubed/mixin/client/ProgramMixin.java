package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;

import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;

@Mixin(Program.class)
public class ProgramMixin {
	@ModifyArg(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;process(Ljava/lang/String;)Ljava/util/List;"))
	private static String patchShaderSource(String source, @Local(argsOnly = true, ordinal = 0) String name, @Local(argsOnly = true) Program.Type type) {
		return ShaderPatcher.tryPatch(source, name + type.getExtension()).orElse(source);
	}
}
