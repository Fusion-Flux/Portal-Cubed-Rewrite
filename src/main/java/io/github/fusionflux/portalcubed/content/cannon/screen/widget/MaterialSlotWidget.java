package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip;
import io.github.fusionflux.portalcubed.framework.gui.util.ItemListTooltipComponent;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.TickableWidget;
import io.github.fusionflux.portalcubed.framework.item.TagTranslation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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
		super(x, y, SIZE, SIZE, TagTranslation.translate(tag), TEXTURES, onSelect);

		List<ItemStack> items = BuiltInRegistries.ITEM.get(tag)
				.map(ListBacked::stream)
				.orElseGet(Stream::of)
				.map(Holder::value)
				.map(ItemStack::new)
				.toList();
		this.items = items.isEmpty() ? emptyPlaceholder : items;

		this.tooltip = new AdvancedTooltip(builder -> {
			builder.add(TagTranslation.translate(tag));

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
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		if (this.isActive()) {
			graphics.renderItem(this.getRenderedItem(), this.getX() + OFFSET, this.getY() + OFFSET);
			if (this.isHovered()) {
				PoseStack matrices = graphics.pose();
				matrices.pushPose();
				// extra Z to render on top of the really high side panels
				matrices.translate(0, 0, 500);
				this.tooltip.render(graphics, mouseX, mouseY);
				matrices.popPose();
			}
		}
	}

	private ItemStack getRenderedItem() {
		int index = this.ticks / TICKS_PER_ITEM;
		return this.items.get(index % this.items.size());
	}
}
