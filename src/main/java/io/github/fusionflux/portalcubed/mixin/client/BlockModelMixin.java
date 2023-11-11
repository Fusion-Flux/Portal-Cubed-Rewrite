package io.github.fusionflux.portalcubed.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.framework.extension.BlockElementExt;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockModel.class)
public abstract class BlockModelMixin {
	@Shadow
	public abstract List<BlockElement> getElements();

	@ModifyReturnValue(method = "bakeFace", at = @At("RETURN"))
	private static BakedQuad addRenderType(BakedQuad quad,
										   BlockElement part, BlockElementFace partFace, TextureAtlasSprite sprite,
										   Direction direction, ModelState transform, ResourceLocation location) {
		RenderMaterial material = ((BlockElementExt) part).pc$renderMaterial();
		if (material != null) {
			((BakedQuadExt) quad).pc$setRenderMaterial(material);
		}
		return quad;
	}
}
