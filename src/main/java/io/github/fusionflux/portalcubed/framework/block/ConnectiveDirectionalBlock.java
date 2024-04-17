package io.github.fusionflux.portalcubed.framework.block;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class ConnectiveDirectionalBlock extends DirectionalBlock {
	public static final MapCodec<ConnectiveDirectionalBlock> CODEC = simpleCodec(ConnectiveDirectionalBlock::new);

	public ConnectiveDirectionalBlock(Properties properties) {
		super(properties);
	}

	@Override
	@NotNull
	protected MapCodec<? extends DirectionalBlock> codec() {
		return CODEC;
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
		if (this.flip(facing, clickedState))
			facing = facing.getOpposite();
		return this.defaultBlockState().setValue(FACING, facing);
	}

	protected boolean flip(Direction facing, BlockState clickedState) {
		if (!clickedState.is(PortalCubedBlockTags.CONNECTING_DIRECTIONAL_BLOCKS))
			return false;

		if (clickedState.hasProperty(FACING)) {
			return clickedState.getValue(FACING) == facing;
		} else if (clickedState.hasProperty(AXIS)) {
			return facing.getAxis() == clickedState.getValue(AXIS);
		} else {
			return true;
		}
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
