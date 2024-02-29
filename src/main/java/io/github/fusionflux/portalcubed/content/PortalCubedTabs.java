package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class PortalCubedTabs {


	public static final ResourceKey<CreativeModeTab> TEST_ELEMENTS = create("test_elements", builder -> {
		builder.icon(() -> new ItemStack(PortalCubedItems.PORTAL_GUN));
		builder.displayItems((params, output) -> {
			output.accept(PortalCubedItems.HAMMER);
			output.accept(PortalCubedItems.PORTAL_GUN);
			output.accept(PortalCubedBlocks.PORTAL_1_FLOOR_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.FLOOR_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.CUBE_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.OLD_AP_FLOOR_BUTTON_BLOCK);
			output.accept(PropType.PORTAL_1_STORAGE_CUBE.item);
			output.accept(PropType.PORTAL_1_COMPANION_CUBE.item);
			output.accept(PropType.STORAGE_CUBE.item);
			output.accept(PropType.COMPANION_CUBE.item);
			output.accept(PropType.OLD_AP_CUBE.item);
			output.accept(PropType.RADIO.item);
		});
	});

	public static final ResourceKey<CreativeModeTab> PORTAL_BLOCKS = create("portal_blocks", builder -> {
		builder.icon(() -> new ItemStack(Blocks.DIRT));
		builder.displayItems((params, output) -> {
			output.accept(Blocks.DIRT);

		});
	});

	public static final ResourceKey<CreativeModeTab> PROPS_AND_ITEMS = create("props_and_items", builder -> {
		builder.icon(() -> new ItemStack(PortalCubedItems.HAMMER));
		builder.displayItems((params, output) -> {
			output.accept(PortalCubedItems.HAMMER);
			output.accept(PortalCubedItems.PORTAL_GUN);
			addVariant(output, PortalCubedItems.PORTAL_GUN, 1, "potatos_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 2, "portal_gun_atlas");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 3, "portal_gun_p_body");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 4, "portal_gun_reloaded");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 101, "legacy_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 102, "legacy_portal_gun_atlas");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 103, "legacy_portal_gun_p_body");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 104, "legacy_portal_gun_reloaded");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 201, "mel_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 202, "2006_beta_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 203, "2005_beta_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 204, "bendy_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 205, "blueprint_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 206, "lego_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 207, "damaged_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 208, "revolution_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 301, "2d_portal_gun");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 302, "2d_portal_gun_atlas");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 303, "2d_portal_gun_p_body");
			addVariant(output, PortalCubedItems.PORTAL_GUN, 304, "2d_portal_gun_reloaded");
			output.accept(PropType.PORTAL_1_STORAGE_CUBE.item);
			output.accept(PropType.PORTAL_1_COMPANION_CUBE.item);
			addVariant(output, PropType.PORTAL_1_COMPANION_CUBE.item, 1);
			output.accept(PropType.STORAGE_CUBE.item);
			addVariant(output, PropType.STORAGE_CUBE.item, 2);
			output.accept(PropType.COMPANION_CUBE.item);
			addVariant(output, PropType.COMPANION_CUBE.item, 2);
			output.accept(PropType.OLD_AP_CUBE.item);
			output.accept(PropType.RADIO.item);
			addVariant(output, PropType.RADIO.item, 1);
			addVariant(output, PropType.RADIO.item, 2);
			addVariant(output, PropType.RADIO.item, 3);
			addVariant(output, PropType.RADIO.item, 4);
			output.accept(PropType.MUG.item);
			addVariant(output, PropType.MUG.item, 1);
			addVariant(output, PropType.MUG.item, 2);
			addVariant(output, PropType.MUG.item, 3);
			addVariant(output, PropType.MUG.item, 4);
			addVariant(output, PropType.MUG.item, 5);
			addVariant(output, PropType.MUG.item, 6);
			addVariant(output, PropType.MUG.item, 7);
			output.accept(PropType.CHAIR.item);
			output.accept(PropType.COMPUTER.item);
			output.accept(PropType.CLIPBOARD.item);
			addVariant(output, PropType.CLIPBOARD.item, 1);
			addVariant(output, PropType.CLIPBOARD.item, 2);
			addVariant(output, PropType.CLIPBOARD.item, 3);
			addVariant(output, PropType.CLIPBOARD.item, 4);
			addVariant(output, PropType.CLIPBOARD.item, 5);
			addVariant(output, PropType.CLIPBOARD.item, 6);
			output.accept(PropType.HOOPY.item);
			output.accept(PropType.BEANS.item);
			output.accept(PropType.JUG.item);
			output.accept(PropType.OIL_DRUM.item);
			addVariant(output, PropType.OIL_DRUM.item, 1);
			addVariant(output, PropType.OIL_DRUM.item, 2);
			addVariant(output, PropType.OIL_DRUM.item, 3);
			output.accept(PropType.COOKING_POT.item);
			output.accept(PropType.LIL_PINEAPPLE.item);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 1);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 2);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 3);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 4);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 5);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 6);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 7);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 8);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 9);
			addVariant(output, PropType.LIL_PINEAPPLE.item, 10);
			output.accept(PropType.THE_TACO.item);

		});
	});

	private static void addVariant(CreativeModeTab.Output output, Item item, int cmd) {
		ItemStack stack = new ItemStack(item);
		stack.getOrCreateTag().putInt("CustomModelData", cmd);
		output.accept(stack);
	}

	private static void addVariant(CreativeModeTab.Output output, Item item, int cmd, String lang) {
		ItemStack stack = new ItemStack(item);
		stack.getOrCreateTag().putInt("CustomModelData", cmd);
		Component name = Component.translatable("item.portalcubed." + lang).withStyle(style -> style.withItalic(false));
		stack.setHoverName(name);
		output.accept(stack);
	}

	private static ResourceKey<CreativeModeTab> create(String name, Consumer<CreativeModeTab.Builder> consumer) {
		CreativeModeTab.Builder builder = FabricItemGroup.builder().title(
				Component.translatable("portalcubed.itemGroup." + name)
		);
		consumer.accept(builder);
		return Registry.registerForHolder(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				ResourceKey.create(Registries.CREATIVE_MODE_TAB, PortalCubed.id(name)),
				builder.build()
		).key();
	}

	public static void init() {
	}
}
