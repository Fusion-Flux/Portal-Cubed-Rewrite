package io.github.fusionflux.portalcubed.framework.item;

import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.framework.block.AbstractMultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class MultiBlockItem extends BlockItem {
	public final AbstractMultiBlock block;

	public MultiBlockItem(AbstractMultiBlock block, Properties settings) {
		super(block, settings);
		this.block = block;
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		var direction = state.getValue(AbstractMultiBlock.FACING);
		var rotatedSize = block.size.rotated(direction);

		var horizontalDirection = direction.getAxis().isHorizontal() ? direction.getOpposite() : context.getHorizontalDirection();
		var correctedClickedPos = context.getClickedPos();
		if (horizontalDirection == Direction.SOUTH || horizontalDirection == Direction.WEST)
			correctedClickedPos = correctedClickedPos.relative(horizontalDirection.getClockWise());
			if (horizontalDirection.getAxis().isHorizontal() && direction == Direction.DOWN) correctedClickedPos = correctedClickedPos.below();
		if (direction.getAxis().isVertical() && horizontalDirection.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
			correctedClickedPos = correctedClickedPos.relative(context.getHorizontalDirection());

		var level = context.getLevel();
		var collisionContext = CollisionContext.of(context.getPlayer());
		Predicate<BlockPos> placePredicate = pos -> {
			if (pos.getY() < level.getMinBuildHeight() || pos.getY() > level.getMaxBuildHeight() - 1 || !level.getWorldBorder().isWithinBounds(pos))
				return false;
			return level.isUnobstructed(state, pos, collisionContext) && level.getBlockState(pos).canBeReplaced();
		};
		var origin = rotatedSize.moveToFit(
			context,
			rotatedSize.canFit(level, correctedClickedPos, placePredicate) ? correctedClickedPos : context.getClickedPos(),
			placePredicate
		).orElse(null);
		if (origin != null) {
			for (BlockPos pos : BlockPos.betweenClosed(origin, origin.offset(rotatedSize.x() - 1, rotatedSize.y() - 1, rotatedSize.z() - 1))) {
				var relativePos = rotatedSize.relative(origin, pos);
				var quadrantState = block.setX(block.setY(block.setZ(state, relativePos.getZ()), relativePos.getY()), relativePos.getX());
				level.setBlock(pos, quadrantState, Block.UPDATE_ALL_IMMEDIATE);
			}
			return true;
		}

		return false;
	}
}
