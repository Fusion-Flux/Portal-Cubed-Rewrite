package io.github.fusionflux.portalcubed.framework.model.blendmode;

import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import io.github.fusionflux.portalcubed.framework.util.ModelUtil;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlendModeBakedModel extends ForwardingBakedModel {
	public MultiBlendModeBakedModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}

	private void transformQuad(BakedQuad vanilla, MutableQuadView toTransform) {
		BlendMode blendMode = ((BakedQuadExt) vanilla).pc$blendMode();
		if (blendMode != null)
			toTransform.material(RenderMaterials.finder()
					.copyFrom(toTransform.material())
					.blendMode(blendMode)
					.find()
			);
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		ModelUtil.emitVanillaQuads(this.wrapped, false, context, this::transformQuad, state, randomSupplier);
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		ModelUtil.emitVanillaQuads(this.wrapped, true, context, this::transformQuad, null, randomSupplier);
	}
}
