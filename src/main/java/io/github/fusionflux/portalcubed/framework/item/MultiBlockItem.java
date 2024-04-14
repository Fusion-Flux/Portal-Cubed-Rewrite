package io.github.fusionflux.portalcubed.framework.item;

import java.util.HashSet;
import java.util.Set;

import io.github.fusionflux.portalcubed.framework.block.FakeBlockPlaceContext;
import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class MultiBlockItem extends BlockItem {
	public final AbstractMultiBlock multiBlock;

	public MultiBlockItem(AbstractMultiBlock multiBlock, Properties settings) {
		super(multiBlock, settings);
		this.multiBlock = multiBlock;
	}

	private Set<BlockPos> quadrantPlacementTest(BlockPlaceContext context, BlockPos origin, BlockPos collisionOrigin, BlockState state) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		CollisionContext collisionContext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		Set<BlockPos> collisions = new HashSet<>();
		for (var quadrantPos : multiBlock.quadrantIterator(origin, state)) {
			if (
				!level.isUnobstructed(state, quadrantPos, collisionContext) ||
				!level.getWorldBorder().isWithinBounds(quadrantPos) ||
				!level.getBlockState(quadrantPos).canBeReplaced(new FakeBlockPlaceContext(context, quadrantPos))
			) {
				collisions.add(quadrantPos.subtract(collisionOrigin));
			}
		}
		return collisions;
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		Direction facing = state.getValue(AbstractMultiBlock.FACING);
		Direction.Axis facingAxis = facing.getAxis();
		AbstractMultiBlock.Size rotatedSize = multiBlock.size.rotated(facing);

		Direction perspectiveDirection = facingAxis.isHorizontal() ? facing.getOpposite() : context.getHorizontalDirection();
		Direction.Axis perspectiveAxis = perspectiveDirection.getAxis();

		BlockPos.MutableBlockPos origin = context.getClickedPos().mutable();
		BlockPos collisionOrigin = origin.immutable();

		if (perspectiveDirection == Direction.SOUTH || perspectiveDirection == Direction.WEST)
			origin.move(perspectiveDirection.getClockWise());
		if (facingAxis.isVertical() && perspectiveDirection.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
			origin.move(perspectiveDirection);

		Set<BlockPos> collisions = quadrantPlacementTest(context, origin, collisionOrigin, state);
		if (!collisions.isEmpty()) {
			Vec3 collisionNormal = new Vec3( 0, 0, 0);
			for (BlockPos collisionDelta : collisions) {
				collisionNormal = collisionNormal.add(Vec3.atLowerCornerOf(collisionDelta));
			}

			collisionNormal = collisionNormal
					.scale(1d / collisions.size())
					.normalize();
			boolean collideX = Math.abs(collisionNormal.x) > .5;
			boolean collideY = Math.abs(collisionNormal.y) > .5;
			boolean collideZ = Math.abs(collisionNormal.z) > .5;
			if ((perspectiveAxis == Direction.Axis.X ? collideZ : collideX) && (facingAxis.isHorizontal() ? collideY : (perspectiveAxis == Direction.Axis.X ? collideX : collideZ))) {
				origin.move(facingAxis.isHorizontal() ? Direction.DOWN : perspectiveDirection.getOpposite());
				if (!quadrantPlacementTest(context, origin, collisionOrigin, state).isEmpty())
					origin.move(perspectiveDirection.getCounterClockWise());
			} else {
				if (collideX)
					origin.move(collisionNormal.x < 0 ? Direction.EAST : Direction.WEST);
				if (collideY)
					origin.move(collisionNormal.y < 0 ? Direction.UP : Direction.DOWN);
				if (collideZ)
					origin.move(collisionNormal.z < 0 ? Direction.SOUTH : Direction.NORTH);
			}

			if (!quadrantPlacementTest(context, origin, collisionOrigin, state).isEmpty())
				return false;
		}

		for (BlockPos quadrantPos : multiBlock.quadrantIterator(origin, state)) {
			FakeBlockPlaceContext quadrantPlacementContext = new FakeBlockPlaceContext(context, quadrantPos);
			BlockState quadrantState = multiBlock.getStateForPlacement(quadrantPlacementContext);
			Vec3i relativePos = rotatedSize.relative(origin, quadrantPos);
			quadrantState = multiBlock.setX(quadrantState, relativePos.getX());
			quadrantState = multiBlock.setY(quadrantState, relativePos.getY());
			quadrantState = multiBlock.setZ(quadrantState, relativePos.getZ());
			if (!super.placeBlock(quadrantPlacementContext, quadrantState)) return false;
		}

		return true;
	}
}
