package io.github.fusionflux.portalcubed.content.goo;

import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GooBlock extends LiquidBlock {
	public GooBlock(Properties settings) {
		super(PortalCubedFluids.GOO, settings);
	}

	@Override
	protected VoxelShape getEntityInsideCollisionShape(BlockState state, Level level, BlockPos pos) {
		return this.fluid.getShape(state.getFluidState(), level, pos);
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level instanceof ServerLevel serverLevel) {
			GooFluid.hurt(serverLevel, entity);
		}
	}
}
