package io.github.fusionflux.portalcubed.framework.gui.util;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ItemListTooltipComponent implements ClientTooltipComponent {
	public static final int MAX_ITEMS_WIDE = 8;
	public static final int ITEM_SIZE = 18;

	private final List<ItemStack> items;

	public ItemListTooltipComponent(List<ItemStack> items) {
		this.items = items;
	}

	@Override
	public int getHeight() {
		int rows = 1 + (this.items.size() / (MAX_ITEMS_WIDE + 1));
		return rows * ITEM_SIZE;
	}

	@Override
	public int getWidth(Font textRenderer) {
		int columns = Math.min(this.items.size(), MAX_ITEMS_WIDE);
		return columns * ITEM_SIZE;
	}

	@Override
	public void renderImage(Font textRenderer, int x, int y, GuiGraphics graphics) {
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack stack = this.items.get(i);
			int row = i / MAX_ITEMS_WIDE;
			int col = i % MAX_ITEMS_WIDE;
			graphics.renderItem(stack, col * ITEM_SIZE + x, row * ITEM_SIZE + y);
		}
	}
}
