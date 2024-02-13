package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PortalCubedItemTags {
	public static final TagKey<Item> AGED_CRAFTING_MATERIALS = create("aged_crafting_materials");

	private static TagKey<Item> create(String name) {
		return TagKey.create(Registries.ITEM, PortalCubed.id(name));
	}

	public static void init() {
	}
}
