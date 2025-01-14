package io.github.fusionflux.portalcubed.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.minecraft.Util;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.ShaderProgramConfig;

@Mixin(ShaderManager.class)
public class ShaderManagerMixin {
	@WrapOperation(
			method = "linkProgram",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/CompiledShaderProgram;setupUniforms(Ljava/util/List;Ljava/util/List;)V"
			)
	)
	private static void patchUniforms(
			CompiledShaderProgram instance,
			List<ShaderProgramConfig.Uniform> uniforms,
			List<ShaderProgramConfig.Sampler> samplers,
			Operation<Void> original,
			@Local(argsOnly = true) ShaderProgramConfig config
	) {
		if (ShaderPatcher.shouldPatch(config.vertex() + ".vsh") || ShaderPatcher.shouldPatch(config.fragment() + ".fsh"))
			original.call(instance, Util.make(new ArrayList<>(uniforms), list -> list.add(ShaderPatcher.CLIPPING_PLANE_UNIFORM_CONFIG)), samplers);
		original.call(instance, uniforms, samplers);
	}
}
