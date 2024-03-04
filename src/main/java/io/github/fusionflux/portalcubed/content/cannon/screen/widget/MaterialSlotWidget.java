package io.github.fusionflux.portalcubed.content.cannon.screen.widget;

import com.google.common.collect.Iterators;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.screen.CannonDataHolder;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MaterialSlotWidget extends TexturedStickyButton {
	public static final int SIZE = 22;
	public static final int OFFSET = 3;

	public static final Textures TEXTURES = new Textures(
			PortalCubed.id("construction_cannon/materials_tab/slot"),
			PortalCubed.id("construction_cannon/materials_tab/slot_hover"),
			PortalCubed.id("construction_cannon/materials_tab/slot_selected")
	);

	private final ItemStack rendered;

	public MaterialSlotWidget(TagKey<Item> tag, CannonDataHolder data, Runnable onSelect) {
		this(tag, 0, 0, onSelect);
	}

	public MaterialSlotWidget(TagKey<Item> tag, int x, int y, Runnable onSelect) {
		super(x, y, SIZE, SIZE, translateTag(tag.location()), TEXTURES, onSelect);

		Item renderedItem = BuiltInRegistries.ITEM.getTag(tag)
				.map(named -> Iterators.getNext(named.iterator(), null))
				.map(Holder::value)
				.orElse(Items.BARRIER);
		this.rendered = new ItemStack(renderedItem);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		if (this.isActive()) {
			graphics.renderItem(this.rendered, this.getX() + OFFSET, this.getY() + OFFSET);
		}
	}

	private static Component translateTag(ResourceLocation id) {
		String key = "tag.item." + id.toString()
				.replace(':', '.')
				.replace('/', '.');
		return Component.translatable(key);
	}
}
