package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;

@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {
	@ModifyExpressionValue(
			method = "loadShader",
			at = @At(
					value = "INVOKE",
					target = "Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderParser;parseShader(Ljava/lang/String;Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderConstants;)Ljava/lang/String;"
			)
	)
	private static String patchShaderSource(String src, @Local(argsOnly = true) ResourceLocation name) {
		return ShaderPatcher.tryPatch(src, name.toString()).orElse(src);
	}
}
