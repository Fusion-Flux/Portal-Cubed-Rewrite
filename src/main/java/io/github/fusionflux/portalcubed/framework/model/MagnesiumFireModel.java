package io.github.fusionflux.portalcubed.framework.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.mixin.client.KeyValueConditionAccessor;
import io.github.fusionflux.portalcubed.mixin.client.OrConditionAccessor;
import io.github.fusionflux.portalcubed.mixin.client.SelectorAccessor;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
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
	private final Collection<Direction> directions;

	MagnesiumFireModel(BakedModel wrapped, BakedModel magnesiumVariant, Selector selector) {
		this.wrapped = wrapped;
		this.magnesiumVariant = magnesiumVariant;
		this.directions = directionsFromCondition(((SelectorAccessor) selector).getCondition());
	}

	private static Collection<Direction> directionsFromCondition(Condition condition) {
		if (condition instanceof KeyValueConditionAccessor keyValue && keyValue.getValue().equals("true")) {
			var dir = Direction.byName(keyValue.getKey());
			if (dir != null)
				return Collections.singleton(dir);
		} else if (condition instanceof OrConditionAccessor or) {
			var directions = new ArrayList<Direction>();
			for (var subCondition : or.getConditions()) {
				directions.addAll(directionsFromCondition(subCondition));
			}
			return directions;
		}
		return Collections.singleton(Direction.DOWN);
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		boolean onMagnesium = false;
		for (var dir : directions) {
			onMagnesium |= blockView.getBlockState(pos.relative(dir)).is(PortalCubedBlockTags.MAGNESIUM_FIRE_BASE_BLOCKS);
		}

		if (onMagnesium) {
			magnesiumVariant.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		} else {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		}
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
}
