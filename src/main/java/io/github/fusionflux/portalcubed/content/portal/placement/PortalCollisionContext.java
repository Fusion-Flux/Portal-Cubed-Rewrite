package io.github.fusionflux.portalcubed.content.portal.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Collision context used when shapes are queried by portals, either for portal shots or finding surfaces.
 */
public enum PortalCollisionContext implements CollisionContext {
	INSTANCE;

	@Override
	public boolean isDescending() {
		return false;
	}

	@Override
	public boolean isAbove(VoxelShape shape, BlockPos pos, boolean canAscend) {
		return false;
	}

	@Override
	public boolean isHoldingItem(Item item) {
		return false;
	}

	@Override
	public boolean canStandOnFluid(FluidState fluid1, FluidState fluid2) {
		return false;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, CollisionGetter level, BlockPos pos) {
		return state.getCollisionShape(level, pos, this);
	}
}
