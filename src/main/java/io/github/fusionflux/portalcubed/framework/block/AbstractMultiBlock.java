package io.github.fusionflux.portalcubed.framework.block;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class AbstractMultiBlock extends DirectionalBlock {
	public final Size size;

	protected AbstractMultiBlock(Properties properties) {
		super(properties);

		var sizeProperties = sizeProperties();
		this.size = new Size(Direction.SOUTH, sizeProperties.xMax, sizeProperties.yMax, sizeProperties.zMax);

		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, size.direction));
	}

	public abstract SizeProperties sizeProperties();

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
		sizeProperties().addToBuilder(builder);
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

		public Vec3i relative(Vec3i origin, Vec3i pos) {
			var relative = pos.subtract(origin);
			return switch (direction) {
				case DOWN, UP ->   new Vec3i(relative.getX(), relative.getZ(), relative.getY());
				case WEST, EAST -> new Vec3i(relative.getZ(), relative.getY(), relative.getX());
				default ->         relative;
			};
		}

		public Size rotated(Direction direction) {
			return switch (direction) {
				case DOWN, UP ->   new Size(direction, x, z, y);
				case WEST, EAST -> new Size(direction, z, y, x);
				default ->         new Size(direction, x, y, z);
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

	public record SizeProperties(int xMax, int yMax, int zMax, @Nullable IntegerProperty x, @Nullable IntegerProperty y, @Nullable IntegerProperty z) {
		public void addToBuilder(StateDefinition.Builder<Block, BlockState> builder) {
			if (x != null) builder.add(x);
			if (y != null) builder.add(y);
			if (z != null) builder.add(z);
		}

		public static SizeProperties create(int x, int y, int z) {
			return new SizeProperties(
				x, y, z,
				x > 1 ? IntegerProperty.create("x", 0, x - 1) : null,
				y > 1 ? IntegerProperty.create("y", 0, y - 1) : null,
				z > 1 ? IntegerProperty.create("z", 0, z - 1) : null
			);
		}
	}
}
