package io.github.fusionflux.portalcubed.framework.block.multiblock;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMultiBlock extends DirectionalBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public final SizeProperties sizeProperties;
	public final Size size;

	protected AbstractMultiBlock(Properties properties) {
		super(properties);

		this.sizeProperties = sizeProperties();
		this.size = new Size(Direction.SOUTH, sizeProperties.xMax, sizeProperties.yMax, sizeProperties.zMax);

		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACING, size.direction));
	}

	public abstract SizeProperties sizeProperties();

	public int getX(BlockState state) {
		return sizeProperties.x.map(state::getValue).orElse(0);
	}

	public int getY(BlockState state) {
		return sizeProperties.y.map(state::getValue).orElse(0);
	}

	public int getZ(BlockState state) {
		return sizeProperties.z.map(state::getValue).orElse(0);
	}

	public BlockState setX(BlockState state, int x) {
		return sizeProperties.x.map(prop -> state.setValue(prop, x)).orElse(state);
	}

	public BlockState setY(BlockState state, int y) {
		return sizeProperties.y.map(prop -> state.setValue(prop, y)).orElse(state);
	}

	public BlockState setZ(BlockState state, int z) {
		return sizeProperties.z.map(prop -> state.setValue(prop, z)).orElse(state);
	}

	public boolean isOrigin(BlockState state) {
		return getX(state) == 0 && getY(state) == 0 && getZ(state) == 0;
	}

	public BlockPos getOriginPos(BlockPos pos, BlockState state) {
		int x = -getX(state);
		int y = -getY(state);
		int z = -getZ(state);
		return switch (state.getValue(FACING).getAxis()) {
			case X -> pos.offset(z, y, x);
			case Y -> pos.offset(x, z, y);
			case Z -> pos.offset(x, y, z);
		};
	}

	public Iterable<BlockPos> quadrantIterator(BlockPos pos, BlockState state) {
		var rotatedSize = size.rotated(state.getValue(FACING));
		return BlockPos.betweenClosed(pos, pos.offset(rotatedSize.x - 1, rotatedSize.y - 1, rotatedSize.z - 1));
	}

	public void playSoundAtCenter(SoundEvent sound, double xOff, double yOff, double zOff, float volume, float pitch, BlockPos pos, BlockState state, Level level) {
		var center = size.rotated(state.getValue(FACING)).center(xOff, yOff, zOff)
			.add(pos.getX(), pos.getY(), pos.getZ());
		level.playSound(null, center.x, center.y, center.z, sound, SoundSource.BLOCKS, volume, pitch);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING);
		sizeProperties().x.map(builder::add);
		sizeProperties().y.map(builder::add);
		sizeProperties().z.map(builder::add);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

		var rotatedSize = size.rotated(state.getValue(FACING));
		if (rotatedSize.contains(getOriginPos(pos, state), pos.relative(direction)) && !neighborState.is(this)) {
			world.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, getId(state));
			return Blocks.AIR.defaultBlockState();
		} else {
			return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (player.getAbilities().instabuild) {
			for (var quadrantPos : quadrantIterator(getOriginPos(pos, state), state)) {
				world.destroyBlock(quadrantPos, false, player);
			}
		}
		return state;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, Builder lootParameterBuilder) {
		if (isOrigin(state))
			return super.getDrops(state, lootParameterBuilder);
		return List.of();
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		return defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER).setValue(FACING, ctx.getClickedFace());
	}

	public record Size(Direction direction, int x, int y, int z) {
		private static final EnumMap<Direction, Size> ROTATED = new EnumMap<>(Direction.class);

		public Vec3 center(double xOffset, double yOffset, double zOffset) {
			var axis = direction.getAxis();
			return new Vec3(
				(x / 2) + axis.choose(zOffset, xOffset, xOffset),
				(y / 2) + axis.choose(yOffset, zOffset, yOffset),
				(z / 2) + axis.choose(xOffset, yOffset, zOffset)
			);
		}

		public boolean contains(Vec3i origin, Vec3i pos) {
			int testX = pos.getX();
			int testY = pos.getY();
			int testZ = pos.getZ();
			int minX = origin.getX();
			int minY = origin.getY();
			int minZ = origin.getZ();
			int maxX = minX + x - 1;
			int maxY = minY + y - 1;
			int maxZ = minZ + z - 1;
			return testX >= minX && testX <= maxX &&
				   testZ >= minZ && testZ <= maxZ &&
				   testY >= minY && testY <= maxY;
		}

		public Vec3i relative(Vec3i origin, Vec3i pos) {
			var relative = pos.subtract(origin);
			return switch (direction.getAxis()) {
				case Y -> new Vec3i(relative.getX(), relative.getZ(), relative.getY());
				case X -> new Vec3i(relative.getZ(), relative.getY(), relative.getX());
				case Z -> relative;
			};
		}

		public Size rotated(Direction direction) {
			return ROTATED.computeIfAbsent(direction, $ -> switch (direction.getAxis()) {
				case X -> new Size(direction, z, y, x);
				case Y -> new Size(direction, x, z, y);
				case Z -> new Size(direction, x, y, z);
			});
		}
	}

	public static record SizeProperties(int xMax, int yMax, int zMax, Optional<IntegerProperty> x, Optional<IntegerProperty> y, Optional<IntegerProperty> z) {
		public static SizeProperties create(int x, int y, int z) {
			return new SizeProperties(
				x, y, z,
				x > 1 ? Optional.of(IntegerProperty.create("x", 0, x - 1)) : Optional.empty(),
				y > 1 ? Optional.of(IntegerProperty.create("y", 0, y - 1)) : Optional.empty(),
				z > 1 ? Optional.of(IntegerProperty.create("z", 0, z - 1)) : Optional.empty()
			);
		}
	}
}
