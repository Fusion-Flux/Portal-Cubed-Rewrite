package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PortalCubedItemTags {
	public static final TagKey<Item> WRENCHES = createCommon("wrenches");

	public static final TagKey<Item> AGED_CRAFTING_MATERIALS = create("aged_crafting_materials");

	public static final TagKey<Item> FALL_DAMAGE_RESETTING = create("fall_damage_resetting");

	private static TagKey<Item> create(String name) {
		return TagKey.create(Registries.ITEM, PortalCubed.id(name));
	}

	private static TagKey<Item> createCommon(String name) {
		return TagKey.create(Registries.ITEM, new ResourceLocation("c", name));
	}
}
