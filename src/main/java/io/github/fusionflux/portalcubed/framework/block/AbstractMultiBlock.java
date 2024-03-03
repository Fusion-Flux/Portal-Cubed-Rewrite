package io.github.fusionflux.portalcubed.framework.block;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMultiBlock extends DirectionalBlock {
	public final SizeProperties sizeProperties;
	public final Size size;

	protected AbstractMultiBlock(Properties properties) {
		super(properties);

		this.sizeProperties = sizeProperties();
		this.size = new Size(Direction.SOUTH, sizeProperties.xMax, sizeProperties.yMax, sizeProperties.zMax);

		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, size.direction));
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

	public boolean isOrigin(BlockState state, Level level) {
		return getX(state) == 0 && getY(state) == 0 && getZ(state) == 0;
	}

	public BlockPos getOriginPos(BlockPos pos, BlockState state) {
		int x = getX(state);
		int y = getY(state);
		int z = getZ(state);
		return switch (state.getValue(FACING)) {
			case DOWN, UP ->   pos.subtract(new BlockPos(x, z, y));
			case WEST, EAST -> pos.subtract(new BlockPos(z, y, x));
			default ->         pos.subtract(new BlockPos(x, y, z));
		};
	}

	public Iterable<BlockPos> quadrantIterator(BlockPos pos, BlockState state, Level level) {
		var rotatedSize = size.rotated(state.getValue(FACING));
		return BlockPos.betweenClosed(pos, pos.offset(rotatedSize.x() - 1, rotatedSize.y() - 1, rotatedSize.z() - 1));
	}

	public void playSoundAtCenter(SoundEvent sound, double xOff, double yOff, double zOff, float volume, float pitch, BlockPos pos, BlockState state, Level level) {
		var center = size.rotated(state.getValue(FACING)).center(xOff, yOff, zOff)
			.add(pos.getX(), pos.getY(), pos.getZ());
		level.playSound(null, center.x, center.y, center.z, sound, SoundSource.BLOCKS, volume, pitch);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
		sizeProperties().x.map(builder::add);
		sizeProperties().y.map(builder::add);
		sizeProperties().z.map(builder::add);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.is(newState.getBlock())) {
			if (isOrigin(state, level)) {
				for (BlockPos quadrantPos : quadrantIterator(pos, state, level)) level.destroyBlock(quadrantPos, false);
			} else {
				var originPos = getOriginPos(pos, state);
				level.getBlockState(originPos).onRemove(level, originPos, Blocks.AIR.defaultBlockState(), false);
			}
		}
		super.onRemove(state, level, pos, newState, moved);
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
		return this.defaultBlockState().setValue(FACING, ctx.getClickedFace());
	}

	public record Size(Direction direction, int x, int y, int z) {
		public static final BlockPos.MutableBlockPos TEST_POS = new BlockPos.MutableBlockPos();
		public static final Direction[][] HORIZONTAL_DIRECTIONS = new Direction[][]{
			new Direction[]{Direction.WEST, Direction.EAST},
			new Direction[]{Direction.NORTH, Direction.SOUTH}
		};
		public static final Direction[][] VERTICAL_DIRECTIONS = new Direction[][]{
			new Direction[]{Direction.DOWN, Direction.UP},
			new Direction[]{Direction.NORTH, Direction.SOUTH}
		};

		public Vec3 center(double xOff, double yOff, double zOff) {
			double xOffRotated = direction.getAxis() == Direction.Axis.X ? zOff : xOff;
			double yOffRotated = direction.getAxis() == Direction.Axis.Y ? zOff : yOff;
			double zOffRotated = direction.getAxis() == Direction.Axis.X ? xOff : direction.getAxis() == Direction.Axis.Y ? yOff : zOff;
			return new Vec3((x / 2) + xOffRotated, (y / 2) + yOffRotated, (z / 2) + zOffRotated);
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
			return switch (direction.getAxis()) {
				case Y -> new Size(direction, x, z, y);
				case X -> new Size(direction, z, y, x);
				case Z -> new Size(direction, x, y, z);
			};
		}

		public boolean canFit(Level level, Vec3i origin, Predicate<BlockPos> placePredicate) {
			for (int y = 0; y < this.y; y++) {
				for (int x = 0; x < this.x; x++) {
					for (int z = 0; z < this.z; z++) {
						TEST_POS.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
						if (!placePredicate.test(TEST_POS)) return false;
					}
				}
			}
			return true;
		}

		public Optional<BlockPos> moveToFit(BlockPlaceContext context, BlockPos origin, Predicate<BlockPos> placePredicate) {
			var level = context.getLevel();
			var testOrigin = origin;
			if (canFit(level, testOrigin, placePredicate)) return Optional.of(testOrigin);
			for (int i = 0; i < 2; i++) {
				if (canFit(level, testOrigin, placePredicate)) return Optional.of(testOrigin);
				testOrigin = testOrigin.relative(HORIZONTAL_DIRECTIONS[direction.getAxis() == Direction.Axis.X ? 1 : 0][i]);
				for (int j = 0; j < 2; j++) {
					if (canFit(level, testOrigin, placePredicate)) return Optional.of(testOrigin);
					testOrigin = testOrigin.relative(VERTICAL_DIRECTIONS[direction.getAxis().isVertical() ? 1 : 0][j]);
				}
			}
			return Optional.empty();
		}
	}

	public record SizeProperties(int xMax, int yMax, int zMax, Optional<IntegerProperty> x, Optional<IntegerProperty> y, Optional<IntegerProperty> z) {
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
