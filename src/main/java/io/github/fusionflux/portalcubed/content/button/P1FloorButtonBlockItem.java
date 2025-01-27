package io.github.fusionflux.portalcubed.content.button;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import io.github.fusionflux.portalcubed.framework.item.MultiBlockItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class P1FloorButtonBlockItem extends MultiBlockItem {
	public static boolean easterEgg = true;

	public P1FloorButtonBlockItem(AbstractMultiBlock multiBlock, Properties settings) {
		super(multiBlock, settings);
	}

	@Override
	public Component getName(ItemStack stack) {
		return easterEgg ? Component.translatable("block.portalcubed.floor_button.easter_egg") : super.getName(stack);
	}
}
