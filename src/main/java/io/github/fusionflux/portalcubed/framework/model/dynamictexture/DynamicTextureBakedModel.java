package io.github.fusionflux.portalcubed.framework.model.dynamictexture;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.framework.util.ModelUtil;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.Optionull;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class DynamicTextureBakedModel extends DelegateBakedModel {
	public DynamicTextureBakedModel(BakedModel parent) {
		super(parent);
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		if (blockView.getBlockEntityRenderData(pos) instanceof DynamicTextureRenderData(Map<String, ResourceLocation> map)) {
			ModelUtil.emitVanillaQuads(false, emitter, this.parent, state, randomSupplier, cullTest, (vanilla, toTransform) -> {
				ResourceLocation replacementTexture = Optionull.map(((BakedQuadExt) vanilla).pc$textureReference(), map::get);
				if (replacementTexture != null) {
					ModelUtil.normalizeUV(toTransform, vanilla.getSprite());
					toTransform.spriteBake(ModelUtil.getSprite(replacementTexture), MutableQuadView.BAKE_NORMALIZED);
				}
			});
		} else {
			super.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
		}
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		super.emitItemQuads(emitter, randomSupplier);
	}
}
