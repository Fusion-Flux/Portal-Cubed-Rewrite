package io.github.fusionflux.portalcubed.content.misc;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;

// This exists purely because the default color for dyed items is brown.
public class LongFallBoots extends DyeableArmorItem {
	public LongFallBoots(ArmorMaterial armorMaterial, Type type, Properties properties) {
		super(armorMaterial, type, properties);
	}

	@Override
	public int getColor(ItemStack stack) {
		int color = super.getColor(stack);
		return color == DyeableArmorItem.DEFAULT_LEATHER_COLOR ? 0xFFFFFFFF : color;
	}
}
