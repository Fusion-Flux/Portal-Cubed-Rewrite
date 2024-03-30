package io.github.fusionflux.portalcubed.framework.gui.util;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TagWithCountTooltipComponent implements ClientTooltipComponent {
	public static final int ITEM_SIZE = 18;
	public static final int MILLIS_PER_ITEM = 1000;

	private static final List<ItemStack> emptyPlaceholder = List.of(new ItemStack(Items.BARRIER));

	private final List<ItemStack> items;

	public TagWithCountTooltipComponent(TagKey<Item> tag, int count) {
		List<ItemStack> items = BuiltInRegistries.ITEM.getTag(tag)
				.map(ListBacked::stream)
				.orElseGet(Stream::of)
				.map(Holder::value)
				.map(item -> new ItemStack(item, count))
				.toList();
		this.items = items.isEmpty() ? emptyPlaceholder : items;
	}

	@Override
	public int getHeight() {
		return ITEM_SIZE;
	}

	@Override
	public int getWidth(Font textRenderer) {
		return ITEM_SIZE;
	}

	@Override
	public void renderImage(Font textRenderer, int x, int y, GuiGraphics graphics) {
		ItemStack item = this.getItem();
		graphics.renderItem(item, x, y);
		// always render count, even if 1
		String count = String.valueOf(item.getCount());
		graphics.renderItemDecorations(Minecraft.getInstance().font, item, x, y, count);
	}

	private ItemStack getItem() {
		// ticks are not available here. use system time instead.
		long time = System.currentTimeMillis();
		int index = (int) (time / MILLIS_PER_ITEM);
		return this.items.get(index % this.items.size());
	}
}
