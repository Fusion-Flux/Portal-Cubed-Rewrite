package io.github.fusionflux.portalcubed.content.button;

import com.google.common.collect.Iterables;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CubeButtonBlock extends FloorButtonBlock {
	private static final double NUDGE_SPEED = .6;

	private static final VoxelShaper[][] SHAPES = new VoxelShaper[][]{
		new VoxelShaper[]{
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 6, 16, 3, 16), box(9.5, 3, 9.5, 16, 6, 10.5), box(9.5, 3, 10.5, 10.5, 6, 16)), Direction.UP),
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 6, 10, 3, 16), box(0, 3, 9.5, 6.5, 6, 10.5), box(5.5, 3, 10.5, 6.5, 6, 16)), Direction.UP)
		},
		new VoxelShaper[]{
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 0, 16, 3, 10), box(9.5, 3, 5.5, 16, 6, 6.5), box(9.5, 3, 0, 10.5, 6, 6.5)), Direction.UP),
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 0, 10, 3, 10), box(0, 3, 5.5, 6.5, 6, 6.5), box(5.5, 3, 0, 6.5, 6, 6.5)), Direction.UP)
		}
	};
	private static final VoxelShape BUTTON_SHAPE = box(9.5, 9.5, 3, 16, 16, 7);
	private static final VoxelShaper[] BOTTOM_NO_WALL_SHAPES = new VoxelShaper[]{
		VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 0, 16, 3, 10)), Direction.UP),
		VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 0, 10, 3, 10)), Direction.UP)
	};

	public CubeButtonBlock(Properties properties) {
		super(properties, SHAPES, BUTTON_SHAPE, entity -> entity.getType().is(PortalCubedEntityTags.PRESSES_CUBE_BUTTONS), PortalCubedSounds.FLOOR_BUTTON_PRESS, PortalCubedSounds.FLOOR_BUTTON_RELEASE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction facing = state.getValue(FACING);
		if (getY(state) == 0 && facing.getAxis().isHorizontal() && context instanceof EntityCollisionContext entityContext) {
			Entity entity = entityContext.getEntity();
			if (entity != null && entityPredicate.test(entity)) {
				int x = getX(state);
				VoxelShaper quadrantShape = switch (facing) {
					case NORTH, EAST -> BOTTOM_NO_WALL_SHAPES[x == 1 ? 0 : 1];
					case WEST, SOUTH -> BOTTOM_NO_WALL_SHAPES[x];
					default -> null;
				};
				return quadrantShape.get(facing);
			}
		}
		return super.getCollisionShape(state, world, pos, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if (isOrigin(state)) {
			world.getEntities((Entity) null, getButtonBounds(state.getValue(FACING)).move(pos), entityPredicate)
				.forEach(e -> e.setDeltaMovement(Vec3.ZERO));
		}
	}

	@Override
	protected void entityPressing(BlockState state, Level world, BlockPos pos, Entity entity) {
		super.entityPressing(state, world, pos, entity);

		if (entity instanceof Prop prop && prop.isHeld())
			return;

		Direction facing = state.getValue(FACING);
		AABB buttonBounds = getButtonBounds(facing).move(pos);
		if (Iterables.any(world.getEntities((Entity) null, buttonBounds, entityPredicate), e -> e != entity))
			return;

		Direction.Axis facingAxis = facing.getAxis();
		boolean horizontal = facingAxis.isHorizontal();
		if (horizontal && !entity.isNoGravity())
			return;

		Vec3 nudgeSpeed = new Vec3(
			facingAxis.choose(0, NUDGE_SPEED, NUDGE_SPEED),
			facingAxis.choose(NUDGE_SPEED, 0, NUDGE_SPEED),
			facingAxis.choose(NUDGE_SPEED, NUDGE_SPEED, 0)
		);
		Vec3 nudge = entity.position()
				.vectorTo(buttonBounds
						.getCenter()
						.add(0, horizontal ? -(entity.getBbHeight() / 2) : 0, 0)
				)
				.multiply(nudgeSpeed);
		if (!nudge.equals(Vec3.ZERO))
			entity.setDeltaMovement(nudge);

		entity.setYRot(Math.round(entity.getYRot() / 90) * 90);
	}
}
