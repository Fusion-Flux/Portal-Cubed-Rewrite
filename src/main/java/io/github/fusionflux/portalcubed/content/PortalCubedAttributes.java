package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class PortalCubedAttributes {
	public static final Holder<Attribute> FALL_DAMAGE_ABSORPTION = register(
			"fall_damage_absorption",
			new RangedAttribute("attribute.name.fall_damage_absorption", 0, 0, 1)
	);

	private static Holder<Attribute> register(String name, Attribute attribute) {
		return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, PortalCubed.id(name), attribute);
	}

	public static void init() {
	}
}
