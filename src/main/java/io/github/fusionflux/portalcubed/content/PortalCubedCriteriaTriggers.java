package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.button.EntityOnButtonTrigger;
import io.github.fusionflux.portalcubed.content.misc.ConfigureTestElementTrigger;
import io.github.fusionflux.portalcubed.content.portal.gun.SubmergedTheOperationalEndOfTheDeviceTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class PortalCubedCriteriaTriggers {
	public static final SubmergedTheOperationalEndOfTheDeviceTrigger SUBMERGED_THE_OPERATIONAL_END_OF_THE_DEVICE = register(
			"submerged_the_operational_end_of_the_device", new SubmergedTheOperationalEndOfTheDeviceTrigger()
	);
	public static final ConfigureTestElementTrigger CONFIGURE_TEST_ELEMENT = register(
			"configure_test_element", new ConfigureTestElementTrigger()
	);
	public static final EntityOnButtonTrigger ENTITY_ON_BUTTON = register(
			"entity_on_button", new EntityOnButtonTrigger()
	);

	private static <T extends CriterionTrigger<?>> T register(String name, T trigger) {
		ResourceLocation id = PortalCubed.id(name);
		return Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
	}

	public static void init() {
	}
}
