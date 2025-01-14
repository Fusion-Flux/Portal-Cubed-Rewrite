package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.gun.SubmergedTheOperationalEndOfTheDeviceTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class PortalCubedCriteriaTriggers {
	public static final SubmergedTheOperationalEndOfTheDeviceTrigger SUBMERGED_THE_OPERATIONAL_END_OF_THE_DEVICE = Registry.register(
			BuiltInRegistries.TRIGGER_TYPES,
			PortalCubed.id("submerged_the_operational_end_of_the_device"),
			new SubmergedTheOperationalEndOfTheDeviceTrigger()
	);

	public static void init() {
	}
}
