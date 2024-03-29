package io.github.fusionflux.portalcubed.framework.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class RealDirectionalBlock extends DirectionalBlock {
	public RealDirectionalBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction facing = ctx.getClickedFace();
		BlockPos clickedOn = ctx.getClickedPos().relative(facing.getOpposite());
		BlockState clickedState = ctx.getLevel().getBlockState(clickedOn);
		if (clickedState.is(this) && clickedState.getValue(FACING) == facing)
			facing = facing.getOpposite();
		return this.defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction newFacing = rotation.rotate(state.getValue(FACING));
		return state.setValue(FACING, newFacing);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Direction newFacing = mirror.mirror(state.getValue(FACING));
		return state.setValue(FACING, newFacing);
	}
}
