package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class PortalCubedEntityTags {
	public static final TagKey<EntityType<?>> PRESSES_FLOOR_BUTTONS = create("presses_floor_buttons");

	private static TagKey<EntityType<?>> create(String name) {
		return TagKey.create(Registries.ENTITY_TYPE, PortalCubed.id(name));
	}

	public static void init() {
	}
}
