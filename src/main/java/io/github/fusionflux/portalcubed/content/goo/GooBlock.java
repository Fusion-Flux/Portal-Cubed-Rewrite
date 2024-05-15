package io.github.fusionflux.portalcubed.content.goo;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class GooBlock extends LiquidBlock {
	public GooBlock(FlowingFluid fluid, Properties settings) {
		super(fluid, settings);
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (
				!entity.isAlive() ||
				entity.getType().is(PortalCubedEntityTags.IMMUNE_TO_TOXIC_GOO) ||
				(entity instanceof ItemEntity itemEntity && itemEntity.getItem().is(PortalCubedItemTags.IMMUNE_TO_TOXIC_GOO))
		) return;

		// Toxic goo is in the water tag, using it should be fine here:tm:
		if (entity.getFluidHeight(FluidTags.WATER) > 0d)
			entity.hurt(PortalCubedDamageSources.toxicGoo(level), 10);
	}
}
