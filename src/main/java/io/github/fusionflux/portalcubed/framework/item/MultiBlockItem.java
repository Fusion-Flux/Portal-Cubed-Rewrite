package io.github.fusionflux.portalcubed.framework.item;

import java.util.HashSet;
import java.util.Set;

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

	private Set<Direction> quadrantPlacementTest(BlockPlaceContext context, BlockPos origin, BlockState state) {
		var level = context.getLevel();
		var player = context.getPlayer();
		var collisionContext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		var collidingDirections = new HashSet<Direction>();
		for (var quadrantPos : multiBlock.quadrantIterator(origin, state)) {
			if (
				!level.isUnobstructed(state, quadrantPos, collisionContext) ||
				!level.getWorldBorder().isWithinBounds(quadrantPos) ||
				!level.getBlockState(quadrantPos).canBeReplaced(new FakeBlockPlaceContext(context, quadrantPos))
			) {
				var dir = Direction.getNearest(
					quadrantPos.getX() - origin.getX(),
					quadrantPos.getY() - origin.getY(),
					quadrantPos.getZ() - origin.getZ()
				).getOpposite();
				collidingDirections.add(dir);
			}
		}
		return collidingDirections;
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		var facing = state.getValue(AbstractMultiBlock.FACING);
		var facingAxis = facing.getAxis();
		var rotatedSize = multiBlock.size.rotated(facing);

		var horizontalDirection = facingAxis.isHorizontal() ? facing.getOpposite() : context.getHorizontalDirection();
		var origin = context.getClickedPos().mutable();
		if (facingAxis.isVertical()) {
			if (horizontalDirection == Direction.SOUTH || horizontalDirection == Direction.WEST)
				origin.move(horizontalDirection.getClockWise());
			if (horizontalDirection.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
				origin.move(horizontalDirection);
		}

		var colliding = quadrantPlacementTest(context, origin, state);
		colliding.forEach(origin::move);
		if (!quadrantPlacementTest(context, origin, state).isEmpty())
			return false;

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
