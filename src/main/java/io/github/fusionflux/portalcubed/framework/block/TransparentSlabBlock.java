package io.github.fusionflux.portalcubed.framework.block;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TransparentSlabBlock extends SlabBlock {
	public TransparentSlabBlock(Properties properties) {
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

	private static Optional<Direction> getSlabDirection(SlabType slabType) {
		return slabType == SlabType.BOTTOM ? Optional.of(Direction.DOWN) : slabType == SlabType.TOP ? Optional.of(Direction.UP) : Optional.empty();
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
		if (stateFrom.is(this)) {
			SlabType typeFrom = stateFrom.getValue(TYPE);
			if (!direction.getAxis().isHorizontal() || state.getValue(TYPE) == typeFrom) {
				Direction directionFrom = getSlabDirection(typeFrom).orElse(direction.getOpposite());
				return direction != directionFrom;
			}
		}
		return super.skipRendering(state, stateFrom, direction);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
		return 1.0F;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}
}
