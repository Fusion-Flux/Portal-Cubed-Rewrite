package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class PortalCubedTabs {
	public static final Holder.Reference<CreativeModeTab> TEST_TAB = Registry.registerForHolder(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, PortalCubed.id("test_tab")),
			FabricItemGroup.builder().title(Component.literal("AAAAAAAAAA")).build()
	);

	public static void init() {
	}
}
