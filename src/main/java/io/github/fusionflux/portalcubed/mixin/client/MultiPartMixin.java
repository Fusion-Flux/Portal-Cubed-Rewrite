package io.github.fusionflux.portalcubed.mixin.client;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.model.PortalCubedModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

@Mixin(MultiPart.class)
public class MultiPartMixin {
	@WrapOperation(
		method = "bake",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/block/model/MultiVariant;bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"
		)
	)
	private BakedModel captureBakingSelector(
		MultiVariant instance,
		ModelBaker modelBaker,
		Function<Material, TextureAtlasSprite> textureGetter,
		ModelState rotationContainer,
		ResourceLocation modelId,
		Operation<BakedModel> original,
		@Local Selector selector
	) {
		PortalCubedModelLoadingPlugin.currentSelectorBaking = selector;
		var ret = original.call(instance, modelBaker, textureGetter, rotationContainer, modelId);
		PortalCubedModelLoadingPlugin.currentSelectorBaking = null;
		return ret;
	}
}
