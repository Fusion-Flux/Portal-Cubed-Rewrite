package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.MaterialSlotWidget;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

public class MaterialsTab {
	public static final int ROWS = 4;
	public static final int COLUMNS = 6;
	public static final int SIZE = COLUMNS * ROWS;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static void init(CannonSettingsHolder settings, PanelLayout layout, ScrollbarWidget scrollBar) {
		GridLayout slots = new GridLayout();
		List<TagKey<Item>> materials = getMaterials();
		int rowCount = Mth.positiveCeilDiv(materials.size(), COLUMNS) - ROWS;
		int scrollRowPos = Math.max((int) ((scrollBar.scrollPos() * rowCount) + .5f), 0);
		int i = -(COLUMNS * scrollRowPos);
		scrollBar.active = rowCount > 0;
		if (scrollBar.active)
			scrollBar.scrollRate = 1f / rowCount;
		for (TagKey<Item> tag : materials) {
			if (i >= 0) {
				MaterialSlotWidget slot = new MaterialSlotWidget(tag, () -> {
					slots.visitWidgets(widget -> ((TexturedStickyButton) widget).deselect());
					settings.update(b -> b.setMaterial(tag));
				});

				Optional<TagKey<Item>> material = settings.get().material();
				if (material.isPresent() && material.get() == tag) {
					slot.select();
				}

				slots.addChild(slot, i / COLUMNS, i % COLUMNS);
			}
			++i;
			if (i >= SIZE) break;
		}
		layout.addChild(X_OFF, Y_OFF, slots);
	}

	private static List<TagKey<Item>> getMaterials() {
		// cursed idea: get a weight for a tag by averaging the raw int IDs of its contents
		List<TagKey<Item>> materials = new ArrayList<>(ConstructManager.INSTANCE.getMaterials());
		materials.sort(Comparator.comparingInt(key -> BuiltInRegistries.ITEM.get(key).map(tag -> {
			if (tag.size() == 0)
				return 0;

			int total = 0;
			for (Holder<Item> holder : tag) {
				Item item = holder.value();
				total += BuiltInRegistries.ITEM.getId(item);
			}
			return total / tag.size();
		}).orElse(0)));
		return materials;
	}
}
