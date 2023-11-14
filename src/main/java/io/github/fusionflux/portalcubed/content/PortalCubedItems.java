package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.crowbar.CrowbarItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;

public class PortalCubedItems {
	public static final PortalGunItem PORTAL_GUN = REGISTRAR.items.create("portal_gun", PortalGunItem::new)
			.settings(s -> s.stacksTo(1).fireResistant())
			.build();

	public static final CrowbarItem CROWBAR = REGISTRAR.items.create("crowbar", CrowbarItem::new)
			.settings(s -> s.stacksTo(1))
			.build();

	public static void init() {
	}
}
