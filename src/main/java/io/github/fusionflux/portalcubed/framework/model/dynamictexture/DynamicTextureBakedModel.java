package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.framework.util.ModelUtil;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.Optionull;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class DynamicTextureBakedModel extends ForwardingBakedModel {
	public DynamicTextureBakedModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (blockView.getBlockEntityRenderData(pos) instanceof DynamicTextureRenderData renderData) {
			ModelUtil.emitVanillaQuads(this.wrapped, false, context, (vanilla, toTransform) -> {
				ResourceLocation replacementTexture = Optionull.map(((BakedQuadExt) vanilla).pc$textureReference(), renderData.map()::get);
				if (replacementTexture != null) {
					ModelUtil.normalizeUV(toTransform, vanilla.getSprite());
					toTransform.spriteBake(ModelUtil.getSprite(replacementTexture), MutableQuadView.BAKE_NORMALIZED | MutableQuadView.BAKE_ROTATE_180);
				}
			}, state, randomSupplier);
		} else {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		super.emitItemQuads(stack, randomSupplier, context);
	}
}
