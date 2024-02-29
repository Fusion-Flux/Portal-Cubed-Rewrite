package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonDataHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.MaterialSlotWidget;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MaterialsTab {
	public static final int COLUMNS = 6;
	public static final int ROWS = 6;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static void init(CannonDataHolder data, PanelLayout layout) {
		int i = 0;
		List<MaterialSlotWidget> slots = new ArrayList<>();
		for (TagKey<Item> tag : ConstructManager.INSTANCE.getMaterialsSorted()) {
			int row = i / ROWS;
			int col = i % COLUMNS;
			int slotX = col * MaterialSlotWidget.SIZE + X_OFF;
			int slotY = row * MaterialSlotWidget.SIZE + Y_OFF;
			MaterialSlotWidget slot = new MaterialSlotWidget(
					tag, data,
					() -> {
                        slots.forEach(MaterialSlotWidget::deselect);
						data.update(settings -> settings.withConstruct(
								ConstructManager.INSTANCE.getConstructSetsForMaterial(tag).stream()
										.limit(1)
										.map(ConstructManager.INSTANCE::getId)
										.filter(Objects::nonNull)
										.findFirst()
						));
                    }
			);
			layout.addChild(slotX, slotY, slot);
			slots.add(slot);
			i++;
		}
	}
}
