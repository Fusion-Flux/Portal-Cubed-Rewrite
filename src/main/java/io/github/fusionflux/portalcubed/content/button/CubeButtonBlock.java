package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class CubeButtonBlock extends FloorButtonBlock {
	private static final double NUDGE_SPEED = .6;

	public CubeButtonBlock(Properties properties) {
		super(properties, new VoxelShaper[][]{
			new VoxelShaper[]{
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 6, 16, 3, 16)), Direction.UP),
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 6, 10, 3, 16)), Direction.UP)
			},
			new VoxelShaper[]{
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(6, 1, 0, 16, 3, 10)), Direction.UP),
				VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 0, 10, 3, 10)), Direction.UP)
			}
		}, box(9.5, 9.5, 3, 16, 16, 6), entity -> entity.getType().is(PortalCubedEntityTags.PRESSES_CUBE_BUTTONS), PortalCubedSounds.FLOOR_BUTTON_PRESS, PortalCubedSounds.FLOOR_BUTTON_RELEASE);
	}

	@Override
	protected void entityPressing(BlockState state, Level level, BlockPos pos, Entity entity) {
		super.entityPressing(state, level, pos, entity);
		if (entity instanceof Prop prop && prop.getHeldBy().isPresent())
			return;
		if (entity instanceof PathfinderMob pathfinderMob)
			pathfinderMob.getNavigation().stop();
		entity.setYRot(Mth.floor(entity.getYRot() / 90) * 90);
		var facing = state.getValue(FACING);
		var facingAxis = facing.getAxis();
		var buttonBounds = getButtonBounds(facing).move(pos);
		var nudgeSpeed = new Vec3(
			facingAxis.choose(0, NUDGE_SPEED, NUDGE_SPEED),
			0,
			facingAxis.choose(NUDGE_SPEED, NUDGE_SPEED, 0)
		);
		entity.setDeltaMovement(entity.position().vectorTo(buttonBounds.getCenter()).multiply(nudgeSpeed));
	}
}
