package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunColorProvider;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class PortalCubedColorProviders {
	public static void init() {
		ColorProviderRegistry.ITEM.register(new PortalGunColorProvider(), PortalCubedItems.PORTAL_GUN);
	}
}
