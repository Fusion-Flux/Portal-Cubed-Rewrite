package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Map;
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
			output.accept(PortalCubedBlocks.PEDESTAL_BUTTON);
			output.accept(PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON);
			addProp(output, PropType.PORTAL_1_STORAGE_CUBE);
			addProp(output, PropType.PORTAL_1_COMPANION_CUBE);
			addProp(output, PropType.STORAGE_CUBE);
			addProp(output, PropType.COMPANION_CUBE);
			addProp(output, PropType.OLD_AP_CUBE);
			addProp(output, PropType.RADIO);
		});
	});

	 public static final ResourceKey<CreativeModeTab> PORTAL_BLOCKS = create("portal_blocks", builder -> {
		builder.icon(() -> {
			Map<PanelPart, Block> blocks = PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE);
			Block block = blocks.get(PanelPart.HALF);
			return new ItemStack(block);
		});
		builder.displayItems((params, output) -> {
			output.accept(PortalCubedItems.CONSTRUCTION_CANNON);
			output.accept(PortalCubedBlocks.MAGNESIUM_ORE);
			output.accept(PortalCubedBlocks.DEEPSLATE_MAGNESIUM_ORE);
			output.accept(PortalCubedBlocks.RAW_MAGNESIUM_BLOCK);
			output.accept(PortalCubedBlocks.MAGNESIUM_BLOCK);
			for (PanelMaterial material : PanelMaterial.values()) {
				Map<PanelPart, Block> blocks = PortalCubedBlocks.PANELS.get(material);
				for (PanelPart part : PanelPart.values()) {
					if (blocks.containsKey(part)) {
						Block block = blocks.get(part);
						output.accept(block);
					}
				}
			}
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_TILES);
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_TILE_SLAB);
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_TILE_STAIRS);
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_TILE_FACADE);
			output.accept(PortalCubedBlocks.LARGE_BLUE_OFFICE_TILES);
			output.accept(PortalCubedBlocks.LARGE_BLUE_OFFICE_TILE_SLAB);
			output.accept(PortalCubedBlocks.LARGE_BLUE_OFFICE_TILE_STAIRS);
			output.accept(PortalCubedBlocks.LARGE_BLUE_OFFICE_TILE_FACADE);
			output.accept(PortalCubedBlocks.SMALL_BLUE_OFFICE_TILES);
			output.accept(PortalCubedBlocks.SMALL_BLUE_OFFICE_TILE_SLAB);
			output.accept(PortalCubedBlocks.SMALL_BLUE_OFFICE_TILE_STAIRS);
			output.accept(PortalCubedBlocks.SMALL_BLUE_OFFICE_TILE_FACADE);
			output.accept(PortalCubedBlocks.BLACK_OFFICE_TILES);
			output.accept(PortalCubedBlocks.BLACK_OFFICE_TILE_SLAB);
			output.accept(PortalCubedBlocks.BLACK_OFFICE_TILE_STAIRS);
			output.accept(PortalCubedBlocks.BLACK_OFFICE_TILE_FACADE);
			output.accept(PortalCubedBlocks.BROWN_OFFICE_TILES);
			output.accept(PortalCubedBlocks.BROWN_OFFICE_TILE_SLAB);
			output.accept(PortalCubedBlocks.BROWN_OFFICE_TILE_STAIRS);
			output.accept(PortalCubedBlocks.BROWN_OFFICE_TILE_FACADE);
			output.accept(PortalCubedBlocks.GRAY_OFFICE_TILES);
			output.accept(PortalCubedBlocks.GRAY_OFFICE_TILE_SLAB);
			output.accept(PortalCubedBlocks.GRAY_OFFICE_TILE_STAIRS);
			output.accept(PortalCubedBlocks.GRAY_OFFICE_TILE_FACADE);
			output.accept(PortalCubedBlocks.ORANGE_OFFICE_TILES);
			output.accept(PortalCubedBlocks.ORANGE_OFFICE_TILE_SLAB);
			output.accept(PortalCubedBlocks.ORANGE_OFFICE_TILE_STAIRS);
			output.accept(PortalCubedBlocks.ORANGE_OFFICE_TILE_FACADE);

		});


	 });

	public static final ResourceKey<CreativeModeTab> PROPS_AND_ITEMS = create("props_and_items", builder -> {
		builder.icon(() -> new ItemStack(PortalCubedItems.HAMMER));
		builder.displayItems((params, output) -> {
			output.accept(PortalCubedItems.HAMMER);
			output.accept(PortalCubedItems.CONSTRUCTION_CANNON);
			output.accept(PortalCubedItems.RAW_MAGNESIUM);
			output.accept(PortalCubedItems.MAGNESIUM_NUGGET);
			output.accept(PortalCubedItems.MAGNESIUM_INGOT);
			output.accept(PortalCubedBlocks.BLACK_FOREST_CAKE.getCake());

			// ----- portal guns -----

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

			// ----- props -----

			addProp(output, PropType.PORTAL_1_STORAGE_CUBE);

			addProp(output, PropType.PORTAL_1_COMPANION_CUBE);
			addVariant(output, PropType.PORTAL_1_COMPANION_CUBE, 1);

			addProp(output, PropType.STORAGE_CUBE);
			addVariant(output, PropType.STORAGE_CUBE, 2);

			addProp(output, PropType.COMPANION_CUBE);
			addVariant(output, PropType.COMPANION_CUBE, 2);

			addProp(output, PropType.OLD_AP_CUBE);

			addProp(output, PropType.RADIO);

			addVariant(output, PropType.RADIO, 1);
			addVariant(output, PropType.RADIO, 2);
			addVariant(output, PropType.RADIO, 3);
			addVariant(output, PropType.RADIO, 4);

			addProp(output, PropType.MUG);
			addVariant(output, PropType.MUG, 1);
			addVariant(output, PropType.MUG, 2);
			addVariant(output, PropType.MUG, 3);
			addVariant(output, PropType.MUG, 4);
			addVariant(output, PropType.MUG, 5);
			addVariant(output, PropType.MUG, 6);
			addVariant(output, PropType.MUG, 7);

			addProp(output, PropType.CHAIR);
			addProp(output, PropType.COMPUTER);

			addProp(output, PropType.CLIPBOARD);
			addVariant(output, PropType.CLIPBOARD, 1);
			addVariant(output, PropType.CLIPBOARD, 2);
			addVariant(output, PropType.CLIPBOARD, 3);
			addVariant(output, PropType.CLIPBOARD, 4);
			addVariant(output, PropType.CLIPBOARD, 5);
			addVariant(output, PropType.CLIPBOARD, 6);

			addProp(output, PropType.HOOPY);
			addProp(output, PropType.BEANS);
			addProp(output, PropType.JUG);

			addProp(output, PropType.OIL_DRUM);
			addVariant(output, PropType.OIL_DRUM, 1);
			addVariant(output, PropType.OIL_DRUM, 2);
			addVariant(output, PropType.OIL_DRUM, 3);

			addProp(output, PropType.COOKING_POT);

			addProp(output, PropType.LIL_PINEAPPLE);
			addVariant(output, PropType.LIL_PINEAPPLE, 1);
			addVariant(output, PropType.LIL_PINEAPPLE, 2);
			addVariant(output, PropType.LIL_PINEAPPLE, 3);
			addVariant(output, PropType.LIL_PINEAPPLE, 4);
			addVariant(output, PropType.LIL_PINEAPPLE, 5);
			addVariant(output, PropType.LIL_PINEAPPLE, 6);
			addVariant(output, PropType.LIL_PINEAPPLE, 7);
			addVariant(output, PropType.LIL_PINEAPPLE, 8);
			addVariant(output, PropType.LIL_PINEAPPLE, 9);
			addVariant(output, PropType.LIL_PINEAPPLE, 10);

			addProp(output, PropType.THE_TACO);
			addProp(output, PropType.ERROR);
		});
	});

	private static void addProp(CreativeModeTab.Output output, PropType type) {
		output.accept(type.item());
	}

	private static void addVariant(CreativeModeTab.Output output, PropType type, int cmd) {
		addVariant(output, type.item(), cmd);
	}

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
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.addAfter(Items.DEEPSLATE_IRON_ORE, PortalCubedBlocks.MAGNESIUM_ORE);
			entries.addAfter(PortalCubedBlocks.MAGNESIUM_ORE, PortalCubedBlocks.DEEPSLATE_MAGNESIUM_ORE);
			entries.addAfter(Items.RAW_IRON_BLOCK, PortalCubedBlocks.RAW_MAGNESIUM_BLOCK);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.addAfter(Items.CHAIN, PortalCubedBlocks.MAGNESIUM_BLOCK);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.addAfter(Items.RAW_IRON, PortalCubedItems.RAW_MAGNESIUM);
			entries.addAfter(Items.IRON_NUGGET, PortalCubedItems.MAGNESIUM_NUGGET);
			entries.addAfter(Items.IRON_INGOT, PortalCubedItems.MAGNESIUM_INGOT);
		});
	}
}
