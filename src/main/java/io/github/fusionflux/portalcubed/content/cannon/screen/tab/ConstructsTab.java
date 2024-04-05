package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct.ConstructButtonWidget;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton.Textures;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class ConstructsTab {
	public static final int ROWS = 2;
	public static final int COLUMNS = 3;
	public static final int SIZE = COLUMNS * ROWS;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static final int SLOT_SIZE = 44;

	public static final Textures BUTTON_TEXTURES = new Textures(
			PortalCubed.id("construction_cannon/constructs_tab/slot"),
			PortalCubed.id("construction_cannon/constructs_tab/slot_hover"),
			PortalCubed.id("construction_cannon/constructs_tab/slot_selected")
	);

	public static void init(CannonSettingsHolder settings, PanelLayout layout, ScrollbarWidget scrollBar) {
		if (settings.get().material().isEmpty())
			return;
		TagKey<Item> material = settings.get().material().get();

		var buttons = new GridLayout();
		var constructs = ConstructManager.INSTANCE.getConstructSetsForMaterial(material);
		int rowCount = Mth.positiveCeilDiv(constructs.size(), COLUMNS) - ROWS;
		int scrollRowPos = Math.max((int) ((scrollBar.scrollPos() * rowCount) + .5f), 0);
		int i = -(COLUMNS * scrollRowPos);
		scrollBar.scrollRate = 1 / rowCount;
		scrollBar.active = constructs.size() >= SIZE;
		for (ConstructSet set : constructs) {
			if (i >= 0) {
				ResourceLocation id = ConstructManager.INSTANCE.getId(set);
				ConstructButtonWidget button = new ConstructButtonWidget(() -> {
					buttons.visitWidgets(widget -> ((ConstructButtonWidget) widget).deselect());
					settings.update(s -> s.withConstruct(id));
				}, set, id, material, BUTTON_TEXTURES, SLOT_SIZE);

				Optional<ResourceLocation> selected = settings.get().construct();
				if (selected.isPresent() && selected.get().equals(id))
					button.select();

				buttons.addChild(button, i / COLUMNS, i % COLUMNS);
			}
			if (++i >= SIZE) break;
		}
		layout.addChild(X_OFF, Y_OFF, buttons);
	}
}
