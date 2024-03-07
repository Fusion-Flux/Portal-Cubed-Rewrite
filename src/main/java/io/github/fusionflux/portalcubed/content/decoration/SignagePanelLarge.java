package io.github.fusionflux.portalcubed.content.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

public class SignagePanelLarge extends FaceAttachedHorizontalDirectionalBlock implements SimpleWaterloggedBlock {

	protected static final BooleanProperty WATERLOGGED;
	protected static final VoxelShape FLOOR_AABB;
	protected static final VoxelShape CEILING_AABB;
	protected static final VoxelShape NORTH_AABB;
	protected static final VoxelShape SOUTH_AABB;
	protected static final VoxelShape WEST_AABB;
	protected static final VoxelShape EAST_AABB;

	public SignagePanelLarge(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
	}

	public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		boolean waterlogged = state.getValue(WATERLOGGED);
		switch (state.getValue(FACE)) {
			case FLOOR:
				return FLOOR_AABB;
			case CEILING:
				return CEILING_AABB;
			case WALL:
				VoxelShape shape;
				switch (direction) {
					case UP:
					case DOWN:
					case NORTH:
						shape = NORTH_AABB;
						break;
					case EAST:
						shape = EAST_AABB;
						break;
					case SOUTH:
						shape = SOUTH_AABB;
						break;
					case WEST:
						shape = WEST_AABB;
						break;
					default:
						throw new IncompatibleClassChangeError();
				}
				return shape;
			default:
				return NORTH_AABB;
		}
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, FACE);
	}


	static {
		WATERLOGGED = BlockStateProperties.WATERLOGGED;
		FLOOR_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
		CEILING_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
		NORTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
		SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
		EAST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
		WEST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	}
}
