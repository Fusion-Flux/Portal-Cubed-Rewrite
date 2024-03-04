package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PortalCubedBlockEntityTypes {
	public static final BlockEntityType<PedestalButtonBlockEntity> PEDESTAL_BUTTON = REGISTRAR.blockEntityTypes.simple(
		"pedestal_button",
		PedestalButtonBlockEntity::new,
		PortalCubedBlocks.PEDESTAL_BUTTON, PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON
	);

	public static void init() {
	}
}
