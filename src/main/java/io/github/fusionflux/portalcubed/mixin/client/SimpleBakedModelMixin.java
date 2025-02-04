package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;

@Mixin(SimpleBakedModel.class)
public abstract class SimpleBakedModelMixin {
	@ModifyReturnValue(method = "bakeFace", at = @At("RETURN"))
	private static BakedQuad addCustomFields(BakedQuad quad, BlockElement element, BlockElementFace face, TextureAtlasSprite sprite, Direction facing, ModelState transform) {
		quad.pc$setBlendMode(element.pc$blendMode());
		quad.pc$setTextureReference(face.texture());
		return quad;
	}
}
