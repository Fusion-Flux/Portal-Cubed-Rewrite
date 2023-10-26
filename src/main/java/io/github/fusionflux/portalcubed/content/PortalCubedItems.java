package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import net.minecraft.world.item.Item;

import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class PortalCubedItems {
	public static final Item TEST_ITEM = REGISTRAR.items.create("test_item")
			.settings(QuiltItemSettings::fireproof)
			.group(PortalCubedTabs.TEST_TAB.key())
			.build();

	public static void init() {
	}
}
