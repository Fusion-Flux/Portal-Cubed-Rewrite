package io.github.fusionflux.portalcubed.content.portal.gun;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class PortalGunColorProvider implements ItemColor {
	public static final int ACTIVE_COLOR = 0;
	public static final int SHELL_COLOR = 1;
	public static final int PRIMARY_COLOR = 2;
	public static final int SECONDARY_COLOR = 3;

	public static final int INVALID = -1;

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		PortalGunSettings data = PortalGunItem.getData(stack);
		return switch (tintIndex) {
			case ACTIVE_COLOR -> data.activeData().color();
			case SHELL_COLOR -> getDyedColor(stack);
			case PRIMARY_COLOR -> data.primary().color();
			case SECONDARY_COLOR -> data.effectiveSecondary().color();
			default -> INVALID;
		};
	}

	private int getDyedColor(ItemStack stack) {
		return stack.getItem() instanceof DyeableLeatherItem dyeable ? dyeable.getColor(stack) : INVALID;
	}
}
