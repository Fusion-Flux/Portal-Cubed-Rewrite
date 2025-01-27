package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.util.ShaderPatcher;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;

@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {
	@WrapOperation(method = "loadShader", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderLoader;getShaderSource(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/String;"))
	private static String patchShaderSource(ResourceLocation name, Operation<String> original) {
		String source = original.call(name);
		return ShaderPatcher.tryPatch(source, name.toString()).orElse(source);
	}
}
