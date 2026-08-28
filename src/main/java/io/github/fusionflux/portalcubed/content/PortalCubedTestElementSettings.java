package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class PortalCubedTestElementSettings {
	public static final Identifier PEDESTAL_BUTTON_TIMER = register("pedestal_button/timer");
	public static final Identifier PEDESTAL_BUTTON_BASE_TOGGLE = register("pedestal_button/base/toggle");
	public static final Identifier PEDESTAL_BUTTON_BASE_POSITION = register("pedestal_button/base/position");
	public static final Identifier SMALL_SIGNAGE_QUADRANT_TOGGLE = register("small_signage/quadrant_toggle");
	public static final Identifier SMALL_SIGNAGE_IMAGE = register("small_signage/image");
	public static final Identifier LARGE_SIGNAGE_IMAGE = register("large_signage/image");

	private static Identifier register(String name) {
		Identifier id = PortalCubed.id(name);
		return Registry.register(PortalCubedRegistries.TEST_ELEMENT_SETTINGS, id, id);
	}

	public static void init() {
	}
}
