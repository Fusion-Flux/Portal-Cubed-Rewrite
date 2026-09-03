package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import java.util.List;
import java.util.stream.Stream;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;
import io.github.fusionflux.portalcubed.framework.gui.util.ItemListTooltipComponent;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MaterialSlotWidget extends TexturedStickyButton implements TickableWidget {
	public static final int TICKS_PER_ITEM = 20;
	public static final int SIZE = 22;
	public static final int OFFSET = 3;

	public static final Textures TEXTURES = new Textures(
			PortalCubed.id("construction_cannon/materials_tab/slot"),
			PortalCubed.id("construction_cannon/materials_tab/slot_hover"),
			PortalCubed.id("construction_cannon/materials_tab/slot_selected")
	);

	private static final List<ItemStack> emptyPlaceholder = List.of(new ItemStack(Items.BARRIER));

	private final List<ItemStack> items;
	private final AdvancedTooltip tooltip;

	private int ticks;

	public MaterialSlotWidget(TagKey<Item> tag, Runnable onSelect) {
		this(tag, 0, 0, onSelect);
	}

	public MaterialSlotWidget(TagKey<Item> tag, int x, int y, Runnable onSelect) {
		super(x, y, SIZE, SIZE, tag.getName(), TEXTURES, onSelect);

		List<ItemStack> items = BuiltInRegistries.ITEM.get(tag)
				.map(ListBacked::stream)
				.orElseGet(Stream::of)
				.map(Holder::value)
				.map(ItemStack::new)
				.toList();
		this.items = items.isEmpty() ? emptyPlaceholder : items;

		this.tooltip = new AdvancedTooltip(builder -> {
			builder.add(tag.getName());

			if (builder.advanced) {
				builder.add(Component.literal('#' + tag.location().toString()).withStyle(ChatFormatting.DARK_GRAY));
			}

			if (this.items == emptyPlaceholder) {
				builder.add(ConstructionCannonScreen.translate("tag.empty").withStyle(ChatFormatting.RED));
			} else {
				builder.add(new ItemListTooltipComponent(this.items));
			}
		});
	}

	@Override
	public void tick() {
		this.ticks++;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
		if (this.isActive()) {
			graphics.item(this.getRenderedItem(), this.getX() + OFFSET, this.getY() + OFFSET);
			if (this.isHovered()) {
				graphics.nextStratum();
				this.tooltip.extractRenderState(graphics, mouseX, mouseY);
			}
		}
	}

	private ItemStack getRenderedItem() {
		int index = this.ticks / TICKS_PER_ITEM;
		return this.items.get(index % this.items.size());
	}
}
