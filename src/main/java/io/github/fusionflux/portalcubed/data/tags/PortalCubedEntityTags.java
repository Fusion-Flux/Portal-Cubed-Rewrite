package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class PortalCubedEntityTags {
	public static final TagKey<EntityType<?>> PORTAL_BLACKLIST = create("portal_blacklist");

	private static TagKey<EntityType<?>> create(String name) {
		return TagKey.create(Registries.ENTITY_TYPE, PortalCubed.id(name));
	}
}
