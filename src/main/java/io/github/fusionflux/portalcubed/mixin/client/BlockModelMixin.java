package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.framework.extension.BlockElementExt;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

@Mixin(BlockModel.class)
public abstract class BlockModelMixin {
	@Shadow
	public abstract List<BlockElement> getElements();

	@ModifyReturnValue(method = "bakeFace", at = @At("RETURN"))
	private static BakedQuad addCustomFields(BakedQuad quad, BlockElement part, BlockElementFace partFace, TextureAtlasSprite sprite, Direction direction, ModelState transform, ResourceLocation location) {
		BlendMode blendMode = ((BlockElementExt) part).pc$blendMode();
		((BakedQuadExt) quad).pc$setBlendMode(blendMode);
		((BakedQuadExt) quad).pc$setTextureReference(partFace.texture);
		return quad;
	}
}
