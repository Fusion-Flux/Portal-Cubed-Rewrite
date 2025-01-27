package io.github.fusionflux.portalcubed.content;

import java.util.Map;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.door.ChamberDoorType;
import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class PortalCubedTabs {
	public static final ResourceKey<CreativeModeTab> TEST_ELEMENTS = create("test_elements", builder -> {
		builder.icon(() -> new ItemStack(PortalCubedItems.PORTAL_GUN));
		builder.displayItems((params, output) -> {
			output.accept(PortalCubedItems.HAMMER);
			addPortalGunVariant(output, "portal_gun", "portal_guns/portal_gun");
			output.accept(PortalCubedItems.ADVANCED_KNEE_REPLACEMENTS);
			output.accept(PortalCubedItems.LONG_FALL_BOOTS);
			output.accept(PortalCubedBlocks.PORTAL_1_FLOOR_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.FLOOR_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.CUBE_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.OLD_AP_FLOOR_BUTTON_BLOCK);
			output.accept(PortalCubedBlocks.SMALL_SIGNAGE);
			output.accept(PortalCubedBlocks.LARGE_SIGNAGE);
			output.accept(PortalCubedBlocks.AGED_SMALL_SIGNAGE);
			output.accept(PortalCubedBlocks.AGED_LARGE_SIGNAGE);
			output.accept(PortalCubedBlocks.PEDESTAL_BUTTON);
			output.accept(PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON);
			for (ChamberDoorType type : ChamberDoorType.values()) {
				PortalCubedBlocks.CHAMBER_DOORS.get(type)
						.values()
						.forEach(output::accept);
			}
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
			output.accept(PortalCubedBlocks.GRAY_CHAMBER_EXTERIOR_FACADE);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_TOP_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_A_TOP_RIGHT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_BOTTOM_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_BOTTOM_RIGHT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_TOP_LEFT);
			output.accept(PortalCubedBlocks.GRAY_2x2_CHAMBER_EXTERIOR_B_TOP_RIGHT);

			output.accept(PortalCubedBlocks.YELLOW_CHAMBER_EXTERIOR);
			output.accept(PortalCubedBlocks.YELLOW_CHAMBER_EXTERIOR_FACADE);
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
			output.accept(PortalCubedItems.CROWBAR);
			output.accept(PortalCubedItems.RAW_MAGNESIUM);
			output.accept(PortalCubedItems.MAGNESIUM_NUGGET);
			output.accept(PortalCubedItems.MAGNESIUM_INGOT);
			output.accept(PortalCubedBlocks.BLACK_FOREST_CAKE.getBase());
			output.accept(PortalCubedItems.LEMON);
			output.accept(PortalCubedItems.LEMONADE);
			output.accept(PortalCubedItems.LEMON_BOAT);
			output.accept(PortalCubedItems.LEMON_CHEST_BOAT);
			output.accept(PortalCubedItems.APERTURE_BANNER_PATTERN);
			output.accept(PortalCubedItems.GOO_BUCKET);
			output.accept(PortalCubedItems.ADVANCED_KNEE_REPLACEMENTS);
			output.accept(PortalCubedItems.LONG_FALL_BOOTS);

			// ----- portal guns -----

			addPortalGunVariant(output, "portal_gun", "portal_guns/portal_gun");
			addPortalGunVariant(output, "potatos_portal_gun", "portal_guns/potatos_portal_gun");
			addPortalGunVariant(output, "portal_gun_atlas", "portal_guns/portal_gun_atlas");
			addPortalGunVariant(output, "portal_gun_p_body", "portal_guns/portal_gun_p_body");
			addPortalGunVariant(output, "portal_gun_reloaded", "portal_guns/portal_gun_reloaded");
			addPortalGunVariant(output, "tiny_potatos_portal_gun", "portal_guns/tiny_potatos_portal_gun");

			addPortalGunVariant(output, "legacy_portal_gun", "portal_guns/legacy_portal_gun");
			addPortalGunVariant(output, "legacy_portal_gun_atlas", "portal_guns/legacy_portal_gun_atlas");
			addPortalGunVariant(output, "legacy_portal_gun_p_body", "portal_guns/legacy_portal_gun_p_body");
			addPortalGunVariant(output, "legacy_portal_gun_reloaded", "portal_guns/legacy_portal_gun_reloaded");

			addPortalGunVariant(output, "2d_portal_gun", "portal_guns/2d_portal_gun");
			addPortalGunVariant(output, "2d_portal_gun_atlas", "portal_guns/2d_portal_gun_atlas");
			addPortalGunVariant(output, "2d_portal_gun_p_body", "portal_guns/2d_portal_gun_p_body");
			addPortalGunVariant(output, "2d_portal_gun_reloaded", "portal_guns/2d_portal_gun_reloaded");

			addPortalGunVariant(output, "mel_portal_gun", "portal_guns/mel_portal_gun");

			addPortalGunVariant(output, "2005_beta_portal_gun", "portal_guns/2005_beta_portal_gun");
			addPortalGunVariant(output, "2006_beta_portal_gun", "portal_guns/2006_beta_portal_gun");

			addPortalGunVariant(output, "damaged_portal_gun", "portal_guns/damaged_portal_gun");
			addPortalGunVariant(output, "painted_portal_gun", "portal_guns/painted_portal_gun");

			addPortalGunVariant(output, "lego_portal_gun", "portal_guns/lego_portal_gun");
			addPortalGunVariant(output, "bendy_portal_gun", "portal_guns/bendy_portal_gun");
			addPortalGunVariant(output, "blueprint_portal_gun", "portal_guns/blueprint_portal_gun");
			addPortalGunVariant(output, "missing_texture_portal_gun", "portal_guns/missing_texture_portal_gun");
			addPortalGunVariant(output, "pistol_portal_gun", "portal_guns/pistol");
			addPortalGunVariant(output, "splash_o_matic", "portal_guns/splash_o_matic");
			addPortalGunVariant(output, "salmon_gun", "portal_guns/salmon_gun");
			addPortalGunVariant(output, "wand", "portal_guns/wand");
			addPortalGunVariant(output, "black_hole_crossbow", "portal_guns/black_hole_crossbow");
			addPortalGunVariant(output, "mr_thingy", "portal_guns/mr_thingy");
			addPortalGunVariant(output, "smithers_portal_gun", "portal_guns/smithers_portal_gun");
			addPortalGunVariant(output, "peashooter", "portal_guns/peashooter");
			addPortalGunVariant(output, "paintbrush", "portal_guns/paintbrush");
			addPortalGunVariant(output, "polaroid", "portal_guns/polaroid");
			addPortalGunVariant(output, "pipis_cannon", "portal_guns/pipis_cannon");
			addPortalGunVariant(output, "portal_gun_rick", "portal_guns/portal_gun_rick");

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
			addPropVariant(output, PropType.BEANS, 1);
			addPropVariant(output, PropType.BEANS, 2);

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

	private static void addPropVariant(CreativeModeTab.Output output, PropType item, int variant) {
		ItemStack stack = new ItemStack(item.item());
		stack.set(PortalCubedDataComponents.PROP_VARIANT, variant);
		output.accept(stack);
	}

	private static void addPortalGunVariant(CreativeModeTab.Output output, String lang, String itemmodel) {
		ItemStack stack = new ItemStack(PortalCubedItems.PORTAL_GUN);
		stack.set(DataComponents.ITEM_NAME, Component.translatable("item.portalcubed." + lang).withStyle(style -> style.withItalic(false)));
		stack.set(DataComponents.ITEM_MODEL, PortalCubed.id(itemmodel)); //replace this with a component for portal gun variant file once added
		//add stuff for setting default primary/secondary portals and crosshair here
		output.accept(stack);
	}

	// Unused for now
	//private static void addItemVariant(CreativeModeTab.Output output, Item item, int cmd, String lang) {
	//	ItemStack stack = new ItemStack(item);
	//	stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of(cmd)));
	//	stack.set(DataComponents.ITEM_NAME, Component.translatable("item.portalcubed." + lang).withStyle(style -> style.withItalic(false)));
	//	output.accept(stack);
	//}

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
			entries.addAfter(Items.GUSTER_BANNER_PATTERN, PortalCubedItems.APERTURE_BANNER_PATTERN);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.addAfter(Items.ENCHANTED_GOLDEN_APPLE, PortalCubedItems.LEMON);
			entries.addAfter(Items.CAKE, PortalCubedBlocks.BLACK_FOREST_CAKE.getBase());
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.addAfter(Blocks.CHERRY_HANGING_SIGN, PortalCubedItems.LEMON_SIGN);
			entries.addAfter(PortalCubedItems.LEMON_SIGN, PortalCubedItems.LEMON_HANGING_SIGN);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.addAfter(Items.CHERRY_CHEST_BOAT, PortalCubedItems.LEMON_BOAT);
			entries.addAfter(PortalCubedItems.LEMON_BOAT, PortalCubedItems.LEMON_CHEST_BOAT);
			entries.addAfter(Items.NETHERITE_HOE, PortalCubedItems.HAMMER);
			entries.addAfter(PortalCubedItems.HAMMER, PortalCubedItems.CROWBAR);
			entries.addAfter(Items.MILK_BUCKET, PortalCubedItems.GOO_BUCKET);
			entries.addAfter(Items.WARPED_FUNGUS_ON_A_STICK, PortalCubedItems.CONSTRUCTION_CANNON);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.addAfter(Items.END_CRYSTAL, PortalCubedItems.LEMONADE);
			entries.addAfter(Items.TURTLE_HELMET, PortalCubedItems.ADVANCED_KNEE_REPLACEMENTS);
			entries.addAfter(PortalCubedItems.ADVANCED_KNEE_REPLACEMENTS, PortalCubedItems.LONG_FALL_BOOTS);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
			if (!entries.shouldShowOpRestrictedItems())
				return;

			entries.addAfter(Items.BARRIER, PortalCubedBlocks.PROP_BARRIER);
			entries.addAfter(Items.DEBUG_STICK, PortalCubedItems.FIZZLEINATOR);
		});
	}
}
