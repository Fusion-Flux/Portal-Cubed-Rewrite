package io.github.fusionflux.portalcubed.framework.block;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.mixin.MultifaceBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SimpleMultifaceBlock extends MultifaceBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<SimpleMultifaceBlock> CODEC = simpleCodec(SimpleMultifaceBlock::new);

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private final MultifaceSpreader spreader = new MultifaceSpreader(this);

	public SimpleMultifaceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends MultifaceBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

		if (!hasAnyFace(state)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			boolean faceRemoved = hasFace(state, direction) && !canAttachTo(world, direction, neighborPos, neighborState);
			if (faceRemoved) {
				var newState = MultifaceBlockAccessor.callRemoveFace(state, getFaceProperty(direction));
				if (hasAnyFace(newState)) {
					var fakeDropState = defaultBlockState()
						.setValue(getFaceProperty(direction), true);
					Block.dropResources(fakeDropState, world, pos, null);
				}
				return newState;
			}
			return state;
		}
	}


	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return context.getItemInHand().is(asItem()) && super.canBeReplaced(state, context);
	}

	@Override
	public MultifaceSpreader getSpreader() {
		return spreader;
	}
}
