package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.Tickable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MaterialSlotWidget extends TexturedStickyButton implements Tickable {
	public static final int SIZE = 22;
	public static final int OFFSET = 3;

	public static final Textures TEXTURES = new Textures(
			PortalCubed.id("construction_cannon/materials_tab/slot"),
			PortalCubed.id("construction_cannon/materials_tab/slot_hover"),
			PortalCubed.id("construction_cannon/materials_tab/slot_selected")
	);

	private static final List<ItemStack> emptyPlaceholder = List.of(new ItemStack(Items.BARRIER));

	private final List<ItemStack> items;

	private int ticks;

	public MaterialSlotWidget(TagKey<Item> tag, Runnable onSelect) {
		this(tag, 0, 0, onSelect);
	}

	public MaterialSlotWidget(TagKey<Item> tag, int x, int y, Runnable onSelect) {
		super(x, y, SIZE, SIZE, translateTag(tag.location()), TEXTURES, onSelect);

		List<ItemStack> items = BuiltInRegistries.ITEM.getTag(tag)
				.map(ListBacked::stream)
				.orElseGet(Stream::of)
				.map(Holder::value)
				.map(ItemStack::new)
				.toList();
		this.items = items.isEmpty() ? emptyPlaceholder : items;
	}

	@Override
	public void tick() {
		this.ticks++;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		if (this.isActive()) {
			graphics.renderItem(this.getRenderedItem(), this.getX() + OFFSET, this.getY() + OFFSET);
			if (this.isHoveredOrFocused()) {
				Font font = Minecraft.getInstance().font;
				graphics.renderTooltip(font, List.of(Component.literal("Tooled Tipped")), Optional.empty(), mouseX, mouseY);
			}
		}
	}

	private ItemStack getRenderedItem() {
		int index = this.ticks / 20;
		return this.items.get(index % this.items.size());
	}

	private static Component translateTag(ResourceLocation id) {
		String key = "tag.item." + id.toString()
				.replace(':', '.')
				.replace('/', '.');
		return Component.translatable(key);
	}
}
