package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PortalCubedBlockEntityTypes {
	public static final BlockEntityType<PedestalButtonBlockEntity> PEDESTAL_BUTTON = REGISTRAR.blockEntities.simple(
		"pedestal_button",
		PedestalButtonBlockEntity::new,
		PortalCubedBlocks.PEDESTAL_BUTTON, PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON
	);

	public static final BlockEntityType<LargeSignageBlockEntity> LARGE_SIGNAGE_PANEL = REGISTRAR.blockEntities.simple(
		"large_signage_panel",
		LargeSignageBlockEntity::new,
		PortalCubedBlocks.LARGE_SIGNAGE, PortalCubedBlocks.AGED_LARGE_SIGNAGE
	);

	public static void init() {
	}
}
