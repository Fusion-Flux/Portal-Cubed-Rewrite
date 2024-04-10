package io.github.fusionflux.portalcubed.framework.model;

import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

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
	private final Iterable<Direction> directions;

	MagnesiumFireModel(BakedModel wrapped, BakedModel magnesiumVariant, Selector selector) {
		this.wrapped = wrapped;
		this.magnesiumVariant = magnesiumVariant;
		this.directions = directionsFromCondition(((SelectorAccessor) selector).getCondition());
	}

	private Iterable<Direction> directionsFromCondition(Condition condition) {
		if (condition instanceof KeyValueConditionAccessor keyValue && Boolean.parseBoolean(keyValue.getValue())) {
			return Collections.singleton(Direction.byName(keyValue.getKey()));
		} else if (condition instanceof OrConditionAccessor or) {
			return Iterables.concat(Streams.stream(or.getConditions())
				.map(this::directionsFromCondition)
				.collect(Collectors.toUnmodifiableSet()));
		}
		return Collections.singleton(Direction.DOWN);
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		var testPos = new BlockPos.MutableBlockPos();
		for (var dir : directions) {
			testPos.set(pos).move(dir);
			if (blockView.getBlockState(testPos).is(PortalCubedBlockTags.MAGNESIUM_FIRE_BASE_BLOCKS)) {
				magnesiumVariant.emitBlockQuads(blockView, state, pos, randomSupplier, context);
				return;
			}
		}
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
}
