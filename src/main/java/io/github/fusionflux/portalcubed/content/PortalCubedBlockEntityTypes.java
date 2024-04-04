package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignagePanelBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PortalCubedBlockEntityTypes {
	public static final BlockEntityType<PedestalButtonBlockEntity> PEDESTAL_BUTTON = REGISTRAR.blockEntities.simple(
		"pedestal_button",
		PedestalButtonBlockEntity::new,
		PortalCubedBlocks.PEDESTAL_BUTTON, PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON
	);

	public static final BlockEntityType<LargeSignagePanelBlockEntity> LARGE_SIGNAGE_PANEL = REGISTRAR.blockEntities.simple(
		"large_signage_panel",
		LargeSignagePanelBlockEntity::new,
		PortalCubedBlocks.LARGE_SIGNAGE_PANEL
	);

	public static void init() {
	}
}
