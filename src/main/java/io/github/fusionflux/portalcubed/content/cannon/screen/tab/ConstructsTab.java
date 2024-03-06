package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import io.github.fusionflux.portalcubed.content.cannon.screen.CannonDataHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.ConstructPreviewWidget;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.SortedSet;

public class ConstructsTab {
	public static final int COLUMNS = 4;
	public static final int ROWS = 3;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static final int SLOT_SIZE = 20;

	public static void init(CannonDataHolder data, PanelLayout layout) {
		if (data.get().material().isEmpty())
			return;

		TagKey<Item> material = data.get().material().get();
		SortedSet<ConstructSet> constructs = ConstructManager.INSTANCE.getConstructSetsForMaterial(material);
		int i = 0;
		for (ConstructSet construct : constructs) {
			ConstructPreviewWidget preview = new ConstructPreviewWidget(SLOT_SIZE, true, );
			TexturedStickyButton button = new TexturedStickyButton()
			i++;
		}
	}
}
