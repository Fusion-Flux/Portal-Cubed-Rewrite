package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.menu.ConstructionCannonMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class PortalCubedMenus {
	public static final MenuType<ConstructionCannonMenu> PANEL_PLACER = register("panel_placer", ConstructionCannonMenu::new);

	private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> factory) {
		MenuType<T> type = new MenuType<>(factory, FeatureFlags.VANILLA_SET);
		return Registry.register(BuiltInRegistries.MENU, PortalCubed.id(name), type);
	}

	public static void init() {
	}
}
