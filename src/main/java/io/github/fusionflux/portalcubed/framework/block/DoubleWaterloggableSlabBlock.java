package io.github.fusionflux.portalcubed.framework.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.properties.SlabType;

import org.jetbrains.annotations.Nullable;

// rest of logic handled in SlabBlockMixin
public class DoubleWaterloggableSlabBlock extends SlabBlock {
	public DoubleWaterloggableSlabBlock(Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos blockPos = ctx.getClickedPos();
		BlockState originalState = ctx.getLevel().getBlockState(blockPos);
		// vanilla forcefully sets waterlogged to false here
		if (originalState.is(this))
			return originalState.setValue(TYPE, SlabType.DOUBLE);
		return super.getStateForPlacement(ctx);
	}
}
