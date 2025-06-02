package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.color.JebPortalColor;
import io.github.fusionflux.portalcubed.content.portal.color.PortalColor;
import net.minecraft.core.Registry;

public class PortalCubedPortalColors {
	public static void init() {
	    register("constant", ConstantPortalColor.TYPE);
		register("jeb_", JebPortalColor.TYPE);
	}

	private static void register(String name, PortalColor.Type<?> type) {
		Registry.register(PortalCubedRegistries.PORTAL_COLOR_TYPE, PortalCubed.id(name), type);
	}
}
