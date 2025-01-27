package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class PortalCubedTestElementSettings {
	public static final ResourceLocation PEDESTAL_BUTTON_TIMER = register("pedestal_button/timer");
	public static final ResourceLocation PEDESTAL_BUTTON_BASE_TOGGLE = register("pedestal_button/base/toggle");
	public static final ResourceLocation PEDESTAL_BUTTON_BASE_POSITION = register("pedestal_button/base/position");
	public static final ResourceLocation SMALL_SIGNAGE_QUADRANT_TOGGLE = register("small_signage/quadrant_toggle");
	public static final ResourceLocation SMALL_SIGNAGE_IMAGE = register("small_signage/image");
	public static final ResourceLocation LARGE_SIGNAGE_IMAGE = register("large_signage/image");

	private static ResourceLocation register(String name) {
		ResourceLocation id = PortalCubed.id(name);
		return Registry.register(PortalCubedRegistries.TEST_ELEMENT_SETTINGS, id, id);
	}

	public static void init() {
	}
}
