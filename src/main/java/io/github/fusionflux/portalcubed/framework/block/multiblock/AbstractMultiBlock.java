package io.github.fusionflux.portalcubed.framework.block.multiblock;

import java.util.EnumMap;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.content.PortalCubedStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMultiBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<Direction> FACE = PortalCubedStateProperties.FACE;

	public final SizeProperties sizeProperties;
	public final Size size;

	protected AbstractMultiBlock(Properties properties) {
		super(properties);

		this.sizeProperties = sizeProperties();
		this.size = new Size(Direction.SOUTH, sizeProperties.xMax, sizeProperties.yMax, sizeProperties.zMax);

		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACE, size.direction));
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
		return switch (state.getValue(FACE).getAxis()) {
			case X -> pos.offset(z, y, x);
			case Y -> pos.offset(x, z, y);
			case Z -> pos.offset(x, y, z);
		};
	}

	public Iterable<BlockPos> quadrants(BlockPos pos, BlockState state) {
		Size rotatedSize = size.rotated(state.getValue(FACE));
		return BlockPos.betweenClosed(pos, pos.offset(rotatedSize.x - 1, rotatedSize.y - 1, rotatedSize.z - 1));
	}

	public void playSoundAtCenter(SoundEvent sound, double xOff, double yOff, double zOff, float volume, float pitch, BlockPos pos, BlockState state, Level level) {
		Vec3 center = this.size.rotated(state.getValue(FACE))
				.center(xOff, yOff, zOff)
				.add(pos.getX(), pos.getY(), pos.getZ());
		level.playSound(null, center.x, center.y, center.z, sound, SoundSource.BLOCKS, volume, pitch);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACE);
		sizeProperties().x.map(builder::add);
		sizeProperties().y.map(builder::add);
		sizeProperties().z.map(builder::add);
	}

	@Override
	@NotNull
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.getValue(WATERLOGGED))
			scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

		Size rotatedSize = this.size.rotated(state.getValue(FACE));
		if (rotatedSize.contains(this.getOriginPos(pos, state), neighborPos) && !neighborState.is(this)) {;
			return Blocks.AIR.defaultBlockState();
		} else {
			return super.updateShape(state, world, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
		}
	}

	@Override
	@NotNull
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		// prevents drop from origin quadrant if this quadrant wouldn't drop anything
		if (player.isCreative() || !player.hasCorrectToolForDrops(state)) {
			BlockPos originPos = this.getOriginPos(pos, state);
			if (originPos != pos) {
				BlockState originState = world.getBlockState(originPos);
				world.setBlock(originPos, originState.getFluidState().createLegacyBlock(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);

				// use null instead of the player here or else it will send the packet to everyone but this player
				world.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, originPos, Block.getId(originState));
			}
		}
		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	@NotNull
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	@NotNull
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACE, rotation.rotate(state.getValue(FACE)));
	}

	@Override
	@NotNull
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACE)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		return this.defaultBlockState()
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
				.setValue(FACE, ctx.getClickedFace());
	}

	public record Size(Direction direction, int x, int y, int z) {
		private static final EnumMap<Direction, Size> ROTATED = new EnumMap<>(Direction.class);

		public Vec3 center(double xOffset, double yOffset, double zOffset) {
			Direction.Axis axis = this.direction.getAxis();
			return new Vec3(
					(this.x / 2d) + axis.choose(zOffset, xOffset, xOffset),
					(this.y / 2d) + axis.choose(yOffset, zOffset, yOffset),
					(this.z / 2d) + axis.choose(xOffset, yOffset, zOffset)
			);
		}

		public boolean contains(Vec3i origin, Vec3i pos) {
			int testX = pos.getX();
			int testY = pos.getY();
			int testZ = pos.getZ();
			int minX = origin.getX();
			int minY = origin.getY();
			int minZ = origin.getZ();
			int maxX = minX + this.x - 1;
			int maxY = minY + this.y - 1;
			int maxZ = minZ + this.z - 1;
			return testX >= minX && testX <= maxX &&
					testZ >= minZ && testZ <= maxZ &&
					testY >= minY && testY <= maxY;
		}

		public Vec3i relative(Vec3i origin, Vec3i pos) {
			Vec3i relative = pos.subtract(origin);
			return switch (this.direction.getAxis()) {
				case Y -> new Vec3i(relative.getX(), relative.getZ(), relative.getY());
				case X -> new Vec3i(relative.getZ(), relative.getY(), relative.getX());
				case Z -> relative;
			};
		}

		public Size rotated(Direction direction) {
			return ROTATED.computeIfAbsent(direction, $ -> switch (direction.getAxis()) {
				case X -> new Size(direction, this.z, this.y, this.x);
				case Y -> new Size(direction, this.x, this.z, this.y);
				case Z -> new Size(direction, this.x, this.y, this.z);
			});
		}
	}

	public record SizeProperties(int xMax, int yMax, int zMax, Optional<IntegerProperty> x, Optional<IntegerProperty> y,
								 Optional<IntegerProperty> z) {
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
