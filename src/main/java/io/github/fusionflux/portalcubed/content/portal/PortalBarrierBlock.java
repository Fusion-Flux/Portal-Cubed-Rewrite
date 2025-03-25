package io.github.fusionflux.portalcubed.content.portal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PortalBarrierBlock extends MultifaceBlock {
	public static final MapCodec<PortalBarrierBlock> CODEC = simpleCodec(PortalBarrierBlock::new);
	private static final VoxelShape UP_AABB = Block.box(01.0, 15.0, 1.0, 15.0, 16.0, 15.0);
	private static final VoxelShape DOWN_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
	private static final VoxelShape WEST_AABB = Block.box(0.0, 1.0, 1.0, 1.0, 15.0, 15.0);
	private static final VoxelShape EAST_AABB = Block.box(15.0, 1.0, 1.0, 16.0, 15.0, 15.0);
	private static final VoxelShape NORTH_AABB = Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 1.0);
	private static final VoxelShape SOUTH_AABB = Block.box(1.0, 1.0, 15.0, 15.0, 15.0, 16.0);
	private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		enumMap.put(Direction.NORTH, NORTH_AABB);
		enumMap.put(Direction.EAST, EAST_AABB);
		enumMap.put(Direction.SOUTH, SOUTH_AABB);
		enumMap.put(Direction.WEST, WEST_AABB);
		enumMap.put(Direction.UP, UP_AABB);
		enumMap.put(Direction.DOWN, DOWN_AABB);
	});
	private final ImmutableMap<BlockState, VoxelShape> shapesCache;

	public PortalBarrierBlock(Properties properties) {
		super(properties);
		this.shapesCache = this.getShapeForEachState(PortalBarrierBlock::calculateMultifaceShape);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return state.getFluidState().isEmpty();
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
		return player != null && player.isCreative() && super.canPlaceLiquid(player, level, pos, state, fluid);
	}

	@Override
	public ItemStack pickupBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
		return player != null && player.isCreative() ? super.pickupBlock(player, level, pos, state) : ItemStack.EMPTY;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return context.getItemInHand().is(this.asItem());
	}

	private static VoxelShape calculateMultifaceShape(BlockState state) {
		VoxelShape voxelShape = Shapes.empty();

		for (Direction direction : DIRECTIONS) {
			if (hasFace(state, direction)) {
				voxelShape = Shapes.or(voxelShape, SHAPE_BY_DIRECTION.get(direction));
			}
		}

		return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapesCache.get(state);
	}
}
