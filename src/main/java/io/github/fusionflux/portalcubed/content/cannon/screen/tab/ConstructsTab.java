package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.widget.construct.ConstructButtonWidget;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton;
import io.github.fusionflux.portalcubed.framework.gui.widget.TexturedStickyButton.Textures;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstructsTab {
	public static final int COLUMNS = 3;
	public static final int X_OFF = 15;
	public static final int Y_OFF = 44;

	public static final int SLOT_SIZE = 44;

	public static final Textures BUTTON_TEXTURES = new Textures(
			PortalCubed.id("construction_cannon/constructs_tab/slot"),
			PortalCubed.id("construction_cannon/constructs_tab/slot_hover"),
			PortalCubed.id("construction_cannon/constructs_tab/slot_selected")
	);

	public static void init(CannonSettingsHolder settings, PanelLayout layout) {
		if (settings.get().material().isEmpty())
			return;

		TagKey<Item> material = settings.get().material().get();
		// wrap in list for indexing
		List<ConstructSet> constructs = new ArrayList<>(ConstructManager.INSTANCE.getConstructSetsForMaterial(material));

		List<TexturedStickyButton> buttons = new ArrayList<>();
		for (int i = 0; i < constructs.size(); i++) {
			int row = i / COLUMNS;
			int col = i % COLUMNS;
			int slotX = col * SLOT_SIZE + X_OFF;
			int slotY = row * SLOT_SIZE + Y_OFF;

			ConstructSet set = constructs.get(i);
			ResourceLocation id = ConstructManager.INSTANCE.getId(set);
			ConstructButtonWidget construct = new ConstructButtonWidget(set, id, material, SLOT_SIZE);
			TexturedStickyButton button = new TexturedStickyButton(0, 0, SLOT_SIZE, SLOT_SIZE, CommonComponents.EMPTY, BUTTON_TEXTURES, () -> {
				buttons.forEach(TexturedStickyButton::deselect);
				settings.update(s -> s.withConstruct(id));
			});

			Optional<ResourceLocation> selected = settings.get().construct();
			if (selected.isPresent() && selected.get().equals(id)) {
				button.select();
			}

			buttons.add(button);
			// add construct first to not block button clicks
			layout.addChild(slotX, slotY, construct);
			layout.addChild(slotX, slotY, button);
		}
	}
}
