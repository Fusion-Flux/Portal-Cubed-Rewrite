package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.NonePortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.StandardPortalValidator;
import net.minecraft.core.Registry;

public class PortalCubedPortalValidators {
	public static void init() {
		register("none", NonePortalValidator.TYPE);
		register("standard", StandardPortalValidator.TYPE);
	}

	private static void register(String name, PortalValidator.Type<?> type) {
		Registry.register(PortalCubedRegistries.PORTAL_VALIDATOR_TYPE, PortalCubed.id(name), type);
	}
}
