package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.cannon.menu.ConstructionCannonScreen;
import net.minecraft.client.gui.screens.MenuScreens;

public class PortalCubedScreens {
	public static void init() {
		MenuScreens.register(PortalCubedMenus.PANEL_PLACER, ConstructionCannonScreen::new);
	}
}
