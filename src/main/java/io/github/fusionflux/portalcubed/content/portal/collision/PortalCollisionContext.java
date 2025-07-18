package io.github.fusionflux.portalcubed.content.portal.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public record PortalCollisionContext(Vec3 pc$pos, CollisionContext wrapped) implements PortalAwareCollisionContext {
	@Override
	public boolean isDescending() {
		return this.wrapped.isDescending();
	}

	@Override
	public boolean isAbove(VoxelShape shape, BlockPos pos, boolean canAscend) {
		// h
		return false;
	}

	@Override
	public boolean isHoldingItem(Item item) {
		return this.wrapped.isHoldingItem(item);
	}

	@Override
	public boolean canStandOnFluid(FluidState fluid1, FluidState fluid2) {
		return this.wrapped.canStandOnFluid(fluid1, fluid2);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, CollisionGetter collisionGetter, BlockPos pos) {
		return this.wrapped.getCollisionShape(state, collisionGetter, pos);
	}

	public static PortalCollisionContext wrap(Vec3 pos, CollisionContext wrapped) {
		if (wrapped instanceof PortalCollisionContext wrapper) {
			wrapped = wrapper.wrapped;
		}

		return new PortalCollisionContext(pos, wrapped);
	}
}
