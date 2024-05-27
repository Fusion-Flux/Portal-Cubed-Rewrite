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
import net.minecraft.world.level.block.Blocks;

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
			output.accept(PortalCubedItems.GOO_BUCKET);
		});
	});

	 public static final ResourceKey<CreativeModeTab> PORTAL_CUBED_BLOCKS = create("portal_cubed_blocks", builder -> {
		builder.icon(() -> {
			Map<PanelPart, Block> blocks = PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE);
			Block block = blocks.get(PanelPart.HALF);
			return new ItemStack(block);
		});
		builder.displayItems((params, output) -> {
			output.accept(PortalCubedItems.CONSTRUCTION_CANNON);
			output.accept(PortalCubedBlocks.PROP_BARRIER);
			output.accept(PortalCubedBlocks.MAGNESIUM_ORE);
			output.accept(PortalCubedBlocks.DEEPSLATE_MAGNESIUM_ORE);
			output.accept(PortalCubedBlocks.RAW_MAGNESIUM_BLOCK);
			output.accept(PortalCubedBlocks.MAGNESIUM_BLOCK);
			output.accept(PortalCubedBlocks.LEMON_LOG);
			output.accept(PortalCubedBlocks.LEMON_WOOD);
			output.accept(PortalCubedBlocks.STRIPPED_LEMON_LOG);
			output.accept(PortalCubedBlocks.STRIPPED_LEMON_WOOD);
			output.accept(PortalCubedBlocks.LEMON_PLANKS);
			output.accept(PortalCubedBlocks.LEMON_STAIRS);
			output.accept(PortalCubedBlocks.LEMON_SLAB);
			output.accept(PortalCubedBlocks.LEMON_FENCE);
			output.accept(PortalCubedBlocks.LEMON_FENCE_GATE);
			output.accept(PortalCubedBlocks.LEMON_DOOR);
			output.accept(PortalCubedBlocks.LEMON_TRAPDOOR);
			output.accept(PortalCubedBlocks.LEMON_PRESSURE_PLATE);
			output.accept(PortalCubedBlocks.LEMON_BUTTON);
			output.accept(PortalCubedBlocks.LEMON_LEAVES);
			output.accept(PortalCubedBlocks.LEMON_SAPLING);
			output.accept(PortalCubedBlocks.LEMON_SIGN);
			output.accept(PortalCubedBlocks.LEMON_HANGING_SIGN);
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

			output.accept(PortalCubedBlocks.OFFICE_CONCRETE);
			output.accept(PortalCubedBlocks.OFFICE_CONCRETE_SLAB);
			output.accept(PortalCubedBlocks.OFFICE_CONCRETE_FACADE);

			output.accept(PortalCubedBlocks.BLUE_OFFICE_CONCRETE);
			output.accept(PortalCubedBlocks.BLUE_OFFICE_CONCRETE_SLAB);
			output.accept(PortalCubedBlocks.BLUE_OFFICE_CONCRETE_FACADE);

			output.accept(PortalCubedBlocks.STRIPED_OFFICE_CONCRETE);
			output.accept(PortalCubedBlocks.STRIPED_OFFICE_CONCRETE_SLAB);
			output.accept(PortalCubedBlocks.STRIPED_OFFICE_CONCRETE_FACADE);

			output.accept(PortalCubedBlocks.WHITE_OFFICE_CONCRETE);
			output.accept(PortalCubedBlocks.WHITE_OFFICE_CONCRETE_SLAB);
			output.accept(PortalCubedBlocks.WHITE_OFFICE_CONCRETE_STAIRS);
			output.accept(PortalCubedBlocks.WHITE_OFFICE_CONCRETE_WALL);
			output.accept(PortalCubedBlocks.WHITE_OFFICE_CONCRETE_FACADE);

			output.accept(PortalCubedBlocks.LIGHT_GRAY_OFFICE_CONCRETE);
			output.accept(PortalCubedBlocks.LIGHT_GRAY_OFFICE_CONCRETE_SLAB);
			output.accept(PortalCubedBlocks.LIGHT_GRAY_OFFICE_CONCRETE_STAIRS);
			output.accept(PortalCubedBlocks.LIGHT_GRAY_OFFICE_CONCRETE_WALL);
			output.accept(PortalCubedBlocks.LIGHT_GRAY_OFFICE_CONCRETE_FACADE);

			output.accept(PortalCubedBlocks.VERTICAL_OFFICE_CONCRETE);

			output.accept(PortalCubedBlocks.ELEVATOR_WALL_END);
			output.accept(PortalCubedBlocks.ELEVATOR_WALL_MIDDLE);

			output.accept(PortalCubedBlocks.PLYWOOD);
			output.accept(PortalCubedBlocks.PLYWOOD_SLAB);
			output.accept(PortalCubedBlocks.PLYWOOD_STAIRS);
			output.accept(PortalCubedBlocks.PLYWOOD_WALL);
			output.accept(PortalCubedBlocks.PLYWOOD_FACADE);

			output.accept(PortalCubedBlocks.INSULATION);
			output.accept(PortalCubedBlocks.INSULATION_FACADE);

			output.accept(PortalCubedBlocks.METAL_GRATE);
			output.accept(PortalCubedBlocks.METAL_GRATE_SLAB);
			output.accept(PortalCubedBlocks.METAL_GRATE_FACADE);
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_GRATE);
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_GRATE_SLAB);
			output.accept(PortalCubedBlocks.PORTAL_1_METAL_GRATE_FACADE);
			output.accept(PortalCubedBlocks.OLD_AP_METAL_GRATE);
			output.accept(PortalCubedBlocks.OLD_AP_METAL_GRATE_SLAB);
			output.accept(PortalCubedBlocks.OLD_AP_METAL_GRATE_FACADE);
			output.accept(PortalCubedBlocks.MESH_GRATE);
			output.accept(PortalCubedBlocks.MESH_GRATE_SLAB);
			output.accept(PortalCubedBlocks.MESH_GRATE_FACADE);

			output.accept(PortalCubedBlocks.METAL_PLATING);
			output.accept(PortalCubedBlocks.METAL_PLATING_SLAB);
			output.accept(PortalCubedBlocks.METAL_PLATING_STAIRS);
			output.accept(PortalCubedBlocks.METAL_PLATING_FACADE);
			output.accept(PortalCubedBlocks.CUT_METAL_PLATING);
			output.accept(PortalCubedBlocks.CUT_METAL_PLATING_SLAB);
			output.accept(PortalCubedBlocks.TREAD_PLATE);
			output.accept(PortalCubedBlocks.TREAD_PLATE_SLAB);
			output.accept(PortalCubedBlocks.TREAD_PLATE_FACADE);
			output.accept(PortalCubedBlocks.DIRTY_METAL_PLATING);
			output.accept(PortalCubedBlocks.DIRTY_METAL_PLATING_SLAB);
			output.accept(PortalCubedBlocks.DIRTY_METAL_PLATING_STAIRS);
			output.accept(PortalCubedBlocks.DIRTY_METAL_PLATING_FACADE);
			output.accept(PortalCubedBlocks.DIRTY_CUT_METAL_PLATING);
			output.accept(PortalCubedBlocks.DIRTY_CUT_METAL_PLATING_SLAB);
			output.accept(PortalCubedBlocks.DIRTY_TREAD_PLATE);
			output.accept(PortalCubedBlocks.DIRTY_TREAD_PLATE_SLAB);
			output.accept(PortalCubedBlocks.DIRTY_TREAD_PLATE_FACADE);

			output.accept(PortalCubedBlocks.GRAY_CHAMBER_EXTERIOR);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_TOP_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_TOP_RIGHT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_TOP_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_TOP_RIGHT);

			output.accept(PortalCubedBlocks.YELLOW_CHAMBER_EXTERIOR);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_A_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_A_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_A_TOP_LEFT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_A_TOP_RIGHT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_B_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_B_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_B_TOP_LEFT);
			output.accept(PortalCubedBlocks.YELLOW_2x2_CHAMBER_EXTERIOR_B_TOP_RIGHT);

			output.accept(PortalCubedBlocks.CROSSBAR_PILLAR);
			output.accept(PortalCubedBlocks.SINGLE_2x2_CROSSBAR_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.SINGLE_2x2_CROSSBAR_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.SINGLE_2x2_CROSSBAR_TOP_LEFT);
			output.accept(PortalCubedBlocks.SINGLE_2x2_CROSSBAR_TOP_RIGHT);
			output.accept(PortalCubedBlocks.DOUBLE_2x2_CROSSBAR_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.DOUBLE_2x2_CROSSBAR_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.DOUBLE_2x2_CROSSBAR_TOP_LEFT);
			output.accept(PortalCubedBlocks.DOUBLE_2x2_CROSSBAR_TOP_RIGHT);

			output.accept(PortalCubedBlocks.SEWAGE);

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
			output.accept(PortalCubedItems.LEMON);
			output.accept(PortalCubedItems.LEMONADE);
			output.accept(PortalCubedItems.LEMON_BOAT);
			output.accept(PortalCubedItems.LEMON_CHEST_BOAT);
			output.accept(PortalCubedItems.GOO_BUCKET);

			// ----- portal guns -----

			output.accept(PortalCubedItems.PORTAL_GUN);
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 1, "potatos_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 2, "portal_gun_atlas");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 3, "portal_gun_p_body");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 4, "portal_gun_reloaded");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 101, "legacy_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 102, "legacy_portal_gun_atlas");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 103, "legacy_portal_gun_p_body");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 104, "legacy_portal_gun_reloaded");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 201, "mel_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 202, "2006_beta_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 203, "2005_beta_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 204, "bendy_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 205, "blueprint_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 206, "lego_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 207, "damaged_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 208, "revolution_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 301, "2d_portal_gun");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 302, "2d_portal_gun_atlas");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 303, "2d_portal_gun_p_body");
			addItemVariant(output, PortalCubedItems.PORTAL_GUN, 304, "2d_portal_gun_reloaded");

			// ----- props -----
			output.accept(PortalCubedBlocks.PROP_BARRIER);

			addProp(output, PropType.PORTAL_1_STORAGE_CUBE);

			addProp(output, PropType.PORTAL_1_COMPANION_CUBE);
			addPropVariant(output, PropType.PORTAL_1_COMPANION_CUBE, 1);

			addProp(output, PropType.STORAGE_CUBE);
			addPropVariant(output, PropType.STORAGE_CUBE, 2);

			addProp(output, PropType.COMPANION_CUBE);
			addPropVariant(output, PropType.COMPANION_CUBE, 2);

			addProp(output, PropType.OLD_AP_CUBE);

			addProp(output, PropType.RADIO);

			addPropVariant(output, PropType.RADIO, 1);
			addPropVariant(output, PropType.RADIO, 2);
			addPropVariant(output, PropType.RADIO, 3);
			addPropVariant(output, PropType.RADIO, 4);

			addProp(output, PropType.MUG);
			addPropVariant(output, PropType.MUG, 1);
			addPropVariant(output, PropType.MUG, 2);
			addPropVariant(output, PropType.MUG, 3);
			addPropVariant(output, PropType.MUG, 4);
			addPropVariant(output, PropType.MUG, 5);
			addPropVariant(output, PropType.MUG, 6);
			addPropVariant(output, PropType.MUG, 7);

			addProp(output, PropType.CHAIR);
			addProp(output, PropType.COMPUTER);

			addProp(output, PropType.CLIPBOARD);
			addPropVariant(output, PropType.CLIPBOARD, 1);
			addPropVariant(output, PropType.CLIPBOARD, 2);
			addPropVariant(output, PropType.CLIPBOARD, 3);
			addPropVariant(output, PropType.CLIPBOARD, 4);
			addPropVariant(output, PropType.CLIPBOARD, 5);
			addPropVariant(output, PropType.CLIPBOARD, 6);

			addProp(output, PropType.HOOPY);
			addProp(output, PropType.BEANS);
			addProp(output, PropType.JUG);

			addProp(output, PropType.OIL_DRUM);
			addPropVariant(output, PropType.OIL_DRUM, 1);
			addPropVariant(output, PropType.OIL_DRUM, 2);
			addPropVariant(output, PropType.OIL_DRUM, 3);

			addProp(output, PropType.COOKING_POT);

			addProp(output, PropType.LIL_PINEAPPLE);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 1);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 2);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 3);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 4);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 5);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 6);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 7);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 8);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 9);
			addPropVariant(output, PropType.LIL_PINEAPPLE, 10);

			addProp(output, PropType.THE_TACO);
		});
	});

	private static void addProp(CreativeModeTab.Output output, PropType type) {
		output.accept(type.item());
	}

	private static void addPropVariant(CreativeModeTab.Output output, PropType item, int cmd) {
		ItemStack stack = new ItemStack(item.item());
		stack.getOrCreateTag().putInt("CustomModelData", cmd);
		output.accept(stack);
	}

	private static void addItemVariant(CreativeModeTab.Output output, Item item, int cmd, String lang) {
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
			entries.addAfter(Blocks.CHERRY_LOG, PortalCubedBlocks.LEMON_LOG);
			entries.addAfter(Blocks.CHERRY_LEAVES, PortalCubedBlocks.LEMON_LEAVES);
			entries.addAfter(Blocks.CHERRY_SAPLING, PortalCubedBlocks.LEMON_SAPLING);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.addAfter(Items.CHAIN, PortalCubedBlocks.MAGNESIUM_BLOCK);
			entries.addAfter(Blocks.CHERRY_BUTTON, PortalCubedBlocks.LEMON_LOG);
			entries.addAfter(PortalCubedBlocks.LEMON_LOG, PortalCubedBlocks.LEMON_WOOD);
			entries.addAfter(PortalCubedBlocks.LEMON_WOOD, PortalCubedBlocks.STRIPPED_LEMON_LOG);
			entries.addAfter(PortalCubedBlocks.STRIPPED_LEMON_LOG, PortalCubedBlocks.STRIPPED_LEMON_WOOD);
			entries.addAfter(PortalCubedBlocks.STRIPPED_LEMON_WOOD, PortalCubedBlocks.LEMON_PLANKS);
			entries.addAfter(PortalCubedBlocks.LEMON_PLANKS, PortalCubedBlocks.LEMON_STAIRS);
			entries.addAfter(PortalCubedBlocks.LEMON_STAIRS, PortalCubedBlocks.LEMON_SLAB);
			entries.addAfter(PortalCubedBlocks.LEMON_SLAB, PortalCubedBlocks.LEMON_FENCE);
			entries.addAfter(PortalCubedBlocks.LEMON_FENCE, PortalCubedBlocks.LEMON_FENCE_GATE);
			entries.addAfter(PortalCubedBlocks.LEMON_FENCE_GATE, PortalCubedBlocks.LEMON_DOOR);
			entries.addAfter(PortalCubedBlocks.LEMON_DOOR, PortalCubedBlocks.LEMON_TRAPDOOR);
			entries.addAfter(PortalCubedBlocks.LEMON_TRAPDOOR, PortalCubedBlocks.LEMON_PRESSURE_PLATE);
			entries.addAfter(PortalCubedBlocks.LEMON_PRESSURE_PLATE, PortalCubedBlocks.LEMON_BUTTON);

		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.addAfter(Items.RAW_IRON, PortalCubedItems.RAW_MAGNESIUM);
			entries.addAfter(Items.IRON_NUGGET, PortalCubedItems.MAGNESIUM_NUGGET);
			entries.addAfter(Items.IRON_INGOT, PortalCubedItems.MAGNESIUM_INGOT);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.addAfter(Items.ENCHANTED_GOLDEN_APPLE, PortalCubedItems.LEMON);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.addAfter(Blocks.CHERRY_HANGING_SIGN, PortalCubedItems.LEMON_SIGN);
			entries.addAfter(PortalCubedItems.LEMON_SIGN, PortalCubedItems.LEMON_HANGING_SIGN);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.addAfter(Items.CHERRY_CHEST_BOAT, PortalCubedItems.LEMON_BOAT);
			entries.addAfter(PortalCubedItems.LEMON_BOAT, PortalCubedItems.LEMON_CHEST_BOAT);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.addAfter(Items.END_CRYSTAL, PortalCubedItems.LEMONADE);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
			if (entries.shouldShowOpRestrictedItems())
				entries.addAfter(Items.BARRIER, PortalCubedBlocks.PROP_BARRIER);
		});
	}
}
