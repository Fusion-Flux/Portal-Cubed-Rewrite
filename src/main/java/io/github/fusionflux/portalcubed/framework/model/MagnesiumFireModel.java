package io.github.fusionflux.portalcubed.framework.model;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.mixin.client.KeyValueConditionAccessor;
import io.github.fusionflux.portalcubed.mixin.client.OrConditionAccessor;
import io.github.fusionflux.portalcubed.mixin.client.SelectorAccessor;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MagnesiumFireModel extends ForwardingBakedModel {
	private final BakedModel magnesiumVariant;
	private final Direction direction;

	MagnesiumFireModel(BakedModel wrapped, BakedModel magnesiumVariant, Selector selector) {
		this.wrapped = wrapped;
		this.magnesiumVariant = magnesiumVariant;
		this.direction = directionFromCondition(((SelectorAccessor) selector).getCondition());
	}

	private static Direction directionFromCondition(Condition condition) {
		if (condition instanceof KeyValueConditionAccessor keyValue) {
			var dir = Direction.byName(keyValue.getKey());
			if (dir != null) return dir;
		} else if (condition instanceof OrConditionAccessor or) {
			return directionFromCondition(Iterables.getFirst(or.getConditions(), null));
		}
		return Direction.DOWN;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (blockView.getBlockState(pos.relative(direction)).is(PortalCubedBlockTags.MAGNESIUM_FIRE_BASE_BLOCKS)) {
			magnesiumVariant.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		} else {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		}
	}

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction face, RandomSource rand) {
		throw new UnsupportedOperationException("isVanillaAdapter is false! call emitBlockQuads/emitItemQuads!");
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
}
