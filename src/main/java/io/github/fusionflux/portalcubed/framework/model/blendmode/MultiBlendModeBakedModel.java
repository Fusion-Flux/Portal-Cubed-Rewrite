package io.github.fusionflux.portalcubed.framework.model.blendmode;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.extension.BakedQuadExt;
import io.github.fusionflux.portalcubed.framework.model.RenderMaterials;
import io.github.fusionflux.portalcubed.framework.util.ModelUtil;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlendModeBakedModel extends DelegateBakedModel {
	public MultiBlendModeBakedModel(BakedModel parent) {
		super(parent);
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
	public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
		ModelUtil.emitVanillaQuads(false, emitter, this.parent, state, randomSupplier, cullTest, this::transformQuad);
	}

	@Override
	public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
		ModelUtil.emitVanillaQuads(true, emitter, this.parent, null, randomSupplier, $ -> false, this::transformQuad);
	}
}
