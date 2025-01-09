package io.github.fusionflux.portalcubed.content.goo;

import io.github.fusionflux.portalcubed.content.PortalCubedFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GooBlock extends LiquidBlock {
	public GooBlock(Properties settings) {
		super(PortalCubedFluids.GOO, settings);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		// Toxic goo is in the water tag, using it should be fine here:tm:
		if (entity.getFluidHeight(FluidTags.WATER) > 0d)
			GooFluid.hurt(world, entity);
	}
}
