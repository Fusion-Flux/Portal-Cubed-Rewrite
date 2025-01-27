package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class PortalCubedArmorMaterials {
	public static final ArmorMaterial ADVANCED_KNEE_REPLACEMENTS = new ArmorMaterial(
			ArmorMaterials.IRON.durability(),
			Util.makeEnumMap(ArmorType.class, $ -> 0),
			ArmorMaterials.IRON.enchantmentValue(),
			ArmorMaterials.IRON.equipSound(),
			0,
			0,
			PortalCubedItemTags.REPAIRS_MAGNESIUM_ARMOR,
			createEquipmentAsset("advanced_knee_replacements")
	);
	public static final ArmorMaterial LONG_FALL_BOOTS = new ArmorMaterial(
			ArmorMaterials.IRON.durability(),
			ArmorMaterials.IRON.defense(),
			ArmorMaterials.IRON.enchantmentValue(),
			ArmorMaterials.IRON.equipSound(),
			0,
			0,
			PortalCubedItemTags.REPAIRS_MAGNESIUM_ARMOR,
			createEquipmentAsset("long_fall_boots")
	);

	public static ResourceKey<EquipmentAsset> createEquipmentAsset(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, PortalCubed.id(name));
	}
}
