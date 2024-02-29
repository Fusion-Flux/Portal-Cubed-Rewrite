package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonDataHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.MaterialSlotWidget;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class MaterialsTab {
	public static final int COLUMNS = 6;
	public static final int ROWS = 6;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static void init(CannonDataHolder data, PanelLayout layout) {
		int i = 0;
		List<TagKey<Item>> tests = List.of(
				ItemTags.ACACIA_LOGS,
				ItemTags.AXES,
				ItemTags.BOATS,
				ItemTags.ANVIL,
				ItemTags.STAIRS,
				ItemTags.DOORS,
				ItemTags.STONE_BRICKS,
				ItemTags.IRON_ORES
		);

		List<MaterialSlotWidget> slots = new ArrayList<>();
		for (TagKey<Item> tag : tests) {
			int row = i / ROWS;
			int col = i % COLUMNS;
			int slotX = col * MaterialSlotWidget.SIZE + X_OFF;
			int slotY = row * MaterialSlotWidget.SIZE + Y_OFF;
			MaterialSlotWidget slot = new MaterialSlotWidget(
					tag, data, () -> slots.forEach(MaterialSlotWidget::deselect)
			);
			layout.addChild(slotX, slotY, slot);
			slots.add(slot);
			i++;
		}
	}
}
