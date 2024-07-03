package io.github.fusionflux.portalcubed.content.misc;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class LongFallBootsColorProvider implements ItemColor {
	public static final LongFallBootsColorProvider INSTANCE = new LongFallBootsColorProvider();

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		if (tintIndex == 0 && stack.getItem() instanceof DyeableLeatherItem dyedItem)
			return dyedItem.getColor(stack);
		return -1;
	}
}
