package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import net.minecraft.world.item.Item;

public class PortalCubedItems {
	public static final PortalGunItem PORTAL_GUN = REGISTRAR.items.create("portal_gun", PortalGunItem::new)
			.settings(s -> s.stacksTo(1).fireResistant())
			.build();

	public static final Item MAGNESIUM_INGOT = REGISTRAR.items.create("magnesium_ingot", Item::new)
			.build();

	public static final Item MAGNESIUM_NUGGET = REGISTRAR.items.create("magnesium_nugget", Item::new)
			.build();

	public static final Item RAW_MAGNESIUM = REGISTRAR.items.create("raw_magnesium", Item::new)
			.build();

	public static void init() {
	}
}
