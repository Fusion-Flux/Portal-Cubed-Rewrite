package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.portal.gun_pedestal.PortalGunPedestalBlockEntity;
import io.github.fusionflux.portalcubed.content.portal.gun_pedestal.PortalGunPedestalRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PortalCubedBlockEntityTypes {
	public static final BlockEntityType<PedestalButtonBlockEntity> PEDESTAL_BUTTON = REGISTRAR.blockEntities.simple(
		"pedestal_button",
		PedestalButtonBlockEntity::new,
		PortalCubedBlocks.PEDESTAL_BUTTON, PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON
	);

	public static final BlockEntityType<LargeSignageBlockEntity> LARGE_SIGNAGE = REGISTRAR.blockEntities.simple(
		"large_signage",
		LargeSignageBlockEntity::new,
		PortalCubedBlocks.LARGE_SIGNAGE, PortalCubedBlocks.AGED_LARGE_SIGNAGE
	);

	public static final BlockEntityType<SmallSignageBlockEntity> SMALL_SIGNAGE = REGISTRAR.blockEntities.simple(
		"small_signage",
		SmallSignageBlockEntity::new,
		PortalCubedBlocks.SMALL_SIGNAGE, PortalCubedBlocks.AGED_SMALL_SIGNAGE
	);

	public static final BlockEntityType<PortalGunPedestalBlockEntity> PORTAL_GUN_PEDESTAL = REGISTRAR.blockEntities.create("portal_gun_pedestal", PortalGunPedestalBlockEntity::new)
			.validBlocks(PortalCubedBlocks.PORTAL_GUN_PEDESTAL)
			.renderer(() -> () -> PortalGunPedestalRenderer::new)
			.build();

	public static void init() {
	}
}
