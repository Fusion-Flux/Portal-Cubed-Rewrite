package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.MaterialSlotWidget;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class MaterialsTab {
	public static final int ROWS = 4;
	public static final int COLUMNS = 6;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static void init(CannonSettingsHolder settings, PanelLayout layout, ScrollbarWidget scrollBar) {
		int i = 0;
		var slots = new GridLayout();
		scrollBar.active = false;
		for (TagKey<Item> tag : ConstructManager.INSTANCE.getMaterialsSorted()) {
			MaterialSlotWidget slot = new MaterialSlotWidget(tag, () -> {
				slots.visitWidgets(widget -> ((MaterialSlotWidget) widget).deselect());
				settings.update(s -> s.withMaterial(tag));
			});

			Optional<TagKey<Item>> material = settings.get().material();
			if (material.isPresent() && material.get() == tag) {
				slot.select();
			}

			slots.addChild(slot, i / COLUMNS, i % COLUMNS);
			if (++i >= COLUMNS * ROWS) {
				scrollBar.active = true;
				break;
			}
		}
		layout.addChild(X_OFF, Y_OFF, slots);
	}
}
