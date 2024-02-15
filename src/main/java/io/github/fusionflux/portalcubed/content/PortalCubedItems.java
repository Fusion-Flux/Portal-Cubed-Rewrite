package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;

public class PortalCubedItems {
	public static final PortalGunItem PORTAL_GUN = REGISTRAR.items.create("portal_gun", PortalGunItem::new)
			.settings(s -> s.stacksTo(1).fireResistant())
			.build();

	public static final HammerItem HAMMER = REGISTRAR.items.create("hammer", HammerItem::new)
			.settings(s -> s.stacksTo(1))
			.build();

	public static void init() {
	}
}
