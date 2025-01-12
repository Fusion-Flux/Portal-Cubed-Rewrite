package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PortalCubedItemTags {
	public static final TagKey<Item> WRENCHES = createCommon("wrenches");

	public static final TagKey<Item> AGED_CRAFTING_MATERIALS = create("aged_crafting_materials");

	public static final TagKey<Item> IMMUNE_TO_TOXIC_GOO = create("immune_to_toxic_goo");

	public static final TagKey<Item> REPAIRS_MAGNESIUM_ARMOR = create("repairs_magnesium_armor");

	public static final TagKey<Item> ABSORB_FALL_DAMAGE = create("absorb_fall_damage");
	public static final TagKey<Item> APPLY_SOURCE_PHYSICS = create("apply_source_physics");

	private static TagKey<Item> create(String name) {
		return TagKey.create(Registries.ITEM, PortalCubed.id(name));
	}

	private static TagKey<Item> createCommon(String name) {
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
	}
}
