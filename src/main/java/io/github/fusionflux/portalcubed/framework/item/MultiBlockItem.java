package io.github.fusionflux.portalcubed.framework.item;

import io.github.fusionflux.portalcubed.framework.block.FakeBlockPlaceContext;
import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class MultiBlockItem extends BlockItem {
	public final AbstractMultiBlock multiBlock;

	public MultiBlockItem(AbstractMultiBlock multiBlock, Properties settings) {
		super(multiBlock, settings);
		this.multiBlock = multiBlock;
	}

	private static Direction getDownDirection(Direction.Axis axis) {
		return switch (axis) {
			case X, Z -> Direction.DOWN;
			case Y -> Direction.NORTH;
		};
	}

	private static Direction getLeftDirection(Direction.Axis axis) {
		return switch (axis) {
			case X, Y -> Direction.NORTH;
			case Z -> Direction.WEST;
		};
	}

	private boolean quadrantPlacementTest(BlockPlaceContext context, BlockPos origin, BlockState state) {
		var level = context.getLevel();
		var player = context.getPlayer();
		var collisionContext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		for (var quadrantPos : multiBlock.quadrantIterator(origin, state)) {
			if (
				!level.isUnobstructed(state, quadrantPos, collisionContext) ||
				!level.getWorldBorder().isWithinBounds(quadrantPos) ||
				!level.getBlockState(quadrantPos).canBeReplaced(new FakeBlockPlaceContext(context, quadrantPos))
			) return false;
		}
		return true;
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		var facing = state.getValue(AbstractMultiBlock.FACING);
		var facingAxis = facing.getAxis();
		var rotatedSize = multiBlock.size.rotated(facing);

		var horizontalDirection = facingAxis.isHorizontal() ? facing.getOpposite() : context.getHorizontalDirection();
		var origin = context.getClickedPos();
		if (facingAxis.isVertical()) {
			if (horizontalDirection == Direction.SOUTH || horizontalDirection == Direction.WEST)
				origin = origin.relative(horizontalDirection.getClockWise());
			if (horizontalDirection.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
				origin = origin.relative(horizontalDirection);
		}

		if (!quadrantPlacementTest(context, origin, state)) {
			boolean isCorrectNow = false;
			var downDir = getDownDirection(facingAxis);
			var leftDir = getLeftDirection(facingAxis);
			vertical: for (int i = 0; i < 2; i++) {
				var verticalDir = i == 0 ? downDir : downDir.getOpposite();
				var testOrigin = origin.relative(verticalDir);
				if (quadrantPlacementTest(context, testOrigin, state)) {
					origin = testOrigin;
					isCorrectNow = true;
					break;
				}

				for (int j = 0; j < 2; j++) {
					var horizontalDir = j == 0 ? leftDir : leftDir.getOpposite();
					if (quadrantPlacementTest(context, testOrigin.relative(horizontalDir), state)) {
						origin = testOrigin.relative(horizontalDir);
						isCorrectNow = true;
						break vertical;
					}
				}
			}
			if (!isCorrectNow) return false;
		}

		for (var quadrantPos : multiBlock.quadrantIterator(origin, state)) {
			var relativePos = rotatedSize.relative(origin, quadrantPos);
			state = multiBlock.setX(state, relativePos.getX());
			state = multiBlock.setY(state, relativePos.getY());
			state = multiBlock.setZ(state, relativePos.getZ());
			if (!super.placeBlock(new FakeBlockPlaceContext(context, quadrantPos), state)) return false;
		}

		return true;
	}
}
