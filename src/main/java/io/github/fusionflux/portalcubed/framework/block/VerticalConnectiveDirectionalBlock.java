package io.github.fusionflux.portalcubed.framework.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class VerticalConnectiveDirectionalBlock extends Block {
	public static final MapCodec<VerticalConnectiveDirectionalBlock> CODEC = simpleCodec(VerticalConnectiveDirectionalBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.VERTICAL_DIRECTION;

	public VerticalConnectiveDirectionalBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction clicked = ctx.getClickedFace();
		Direction facing = clicked == Direction.DOWN ? Direction.DOWN : Direction.UP;
		BlockPos clickedOn = ctx.getClickedPos().relative(facing.getOpposite());
		BlockState clickedState = ctx.getLevel().getBlockState(clickedOn);
		if (ConnectiveDirectionalBlock.flip(facing, clickedState))
			facing = facing.getOpposite();
		return this.defaultBlockState().setValue(FACING, facing);
	}
}
