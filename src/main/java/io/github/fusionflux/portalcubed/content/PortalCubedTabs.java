package io.github.fusionflux.portalcubed.content;

import java.util.Map;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.door.ChamberDoorType;
import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshair;
import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshairType;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
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
			output.accept(PortalCubedItems.PORTAL_GUN);
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
			output.accept(PortalCubedBlocks.PORTAL_BARRIER);
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

			addPortalGunSkin(output,
					"portal_gun",
					"default",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"potatos_portal_gun",
					"potatos",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"portal_gun_atlas",
					"atlas",
					"round",
					"round",
					6801401,
					"round",
					5243131
			);
			addPortalGunSkin(output,
					"portal_gun_p_body",
					"p_body",
					"round",
					"round",
					16708972,
					"round",
					9902619
			);
			addPortalGunSkin(output,
					"portal_gun_reloaded",
					"reloaded",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"tiny_potatos_portal_gun",
					"tiny_potatos",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"portal_1_portal_gun",
					"portal_1",
					"round",
					"portal_1_round",
					2396924,
					"portal_1_round",
					16748062
			);

			addPortalGunSkin(output,
					"legacy_portal_gun",
					"legacy",
					"legacy_round",
					"legacy_round",
					1935067,
					"legacy_round",
					14842148
			);
			addPortalGunSkin(output,
					"legacy_portal_gun_atlas",
					"legacy_atlas",
					"legacy_round",
					"legacy_round",
					6801401,
					"legacy_round",
					5243131
			);
			addPortalGunSkin(output,
					"legacy_portal_gun_p_body",
					"legacy_p_body",
					"legacy_round",
					"legacy_round",
					16708972,
					"legacy_round",
					9902619
			);
			addPortalGunSkin(output,
					"legacy_portal_gun_reloaded",
					"legacy_reloaded",
					"legacy_round",
					"legacy_round",
					1935067,
					"legacy_round",
					14842148
			);

			addPortalGunSkin(output,
					"2d_portal_gun",
					"2d",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"2d_portal_gun_atlas",
					"2d_atlas",
					"round",
					"round",
					6801401,
					"round",
					5243131
			);
			addPortalGunSkin(output,
					"2d_portal_gun_p_body",
					"2d_p_body",
					"round",
					"round",
					16708972,
					"round",
					9902619
			);
			addPortalGunSkin(output,
					"2d_portal_gun_reloaded",
					"2d_reloaded",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);

			addPortalGunSkin(output,
					"mel_portal_gun",
					"mel",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);

			addPortalGunSkin(output,
					"2005_beta_portal_gun",
					"2005_beta",
					"beta",
					"round",
					6130645,
					"round",
					13649204
			);
			addPortalGunSkin(output,
					"2006_beta_portal_gun",
					"2006_beta",
					"beta",
					"round",
					9692669,
					"round",
					12820222
			);

			addPortalGunSkin(output,
					"damaged_portal_gun",
					"damaged",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"painted_portal_gun",
					"painted",
					"round",
					"round",
					8712693,
					"round",
					12786459
					);

			addPortalGunSkin(output,
					"lego_portal_gun",
					"lego",
					"round",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"bendy_portal_gun",
					"bendy",
					"round",
					"bendy_portal",
					4953049,
					"bendy_portal",
					14371913
			);
			addPortalGunSkin(output,
					"blueprint_portal_gun",
					"blueprint",
					"round",
					"round",
					6852527,
					"round",
					3565456
			);
			addPortalGunSkin(output,
					"missing_texture_portal_gun",
					"missingno",
					"missingno",
					"missingno",
					16253176,
					"missingno",
					3407871
			);
			addPortalGunSkin(output,
					"pistol_portal_gun",
					"pistol",
					"none",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"splash_o_matic",
					"splash_o_matic",
					"splash_o_matic",
					"ink_splatter",
					3804365,
					"ink_splatter",
					13680136
			);
			addPortalGunSkin(output,
					"salmon_gun",
					"salmon_gun",
					"none",
					"round",
					11220275,
					"round",
					9141545
			);
			addPortalGunSkin(output,
					"wand",
					"wand",
					"none",
					"round",
					5067927,
					"round",
					4879704
			);
			addPortalGunSkin(output,
					"black_hole_crossbow",
					"crossbow",
					"none",
					"round",
					2396924,
					"round",
					16748062
			);
			addPortalGunSkin(output,
					"mr_thingy",
					"mr_thingy",
					"none",
					"round",
					14019049,
					"round",
					5325897
			);
			addPortalGunSkin(output,
					"smithers_portal_gun",
					"smithers",
					"none",
					"round",
					11945201,
					"round",
					15331652
			);
			addPortalGunSkin(output,
					"peashooter",
					"peashooter",
					"none",
					"portal_combat_round",
					8375800,
					"portal_combat_rectangle",
					16250994
			);
			addPortalGunSkin(output,
					"paintbrush",
					"paintbrush",
					"none",
					"round",
					6391489,
					"round",
					14846044
			);
			addPortalGunSkin(output,
					"polaroid",
					"polaroid",
					"none",
					"polaroid",
					-1,
					"polaroid",
					59391
			);
			addPortalGunSkin(output,
					"pipis_cannon",
					"pipis_cannon",
					"none",
					"pipis",
					49151,
					"pipis",
					16776960
			);
			addPortalGunSkin(output,
					"portal_gun_rick",
					"rick",
					"none",
					"spiral",
					9828701,
					"spiral",
					12672505
			);
			addPortalGunSkin(output,
					"wiimote",
					"wiimote",
					"wiimote",
					"round",
					1676543,
					"round",
					16724273
			);


			// ----- props -----
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

	//to-be-replaced with the proper prefabs system once added
	private static void addPortalGunSkin(CreativeModeTab.Output output, String lang, String skin, String crosshairType, String primaryType, Integer primaryColor, String secondaryType, Integer secondaryColor) {
		ItemStack stack = new ItemStack(PortalCubedItems.PORTAL_GUN);
		stack.set(DataComponents.ITEM_NAME, Component.translatable("portal_gun_skin.portalcubed." + lang).withStyle(style -> style.withItalic(false)));
		stack.set(PortalCubedDataComponents.PORTAL_GUN_SETTINGS,
				PortalGunSettings.builder()
						.setSkinId(ResourceKey.create(PortalGunSkin.REGISTRY_KEY, PortalCubed.id(skin)))
						.setCrosshair(new PortalGunCrosshair(ResourceKey.create(PortalGunCrosshairType.REGISTRY_KEY, PortalCubed.id(crosshairType)), true))
						.setPrimary(new PortalSettings(ResourceKey.create(PortalCubedRegistries.PORTAL_TYPE, PortalCubed.id(primaryType)), true, primaryColor, true))
						.setSecondary(new PortalSettings(ResourceKey.create(PortalCubedRegistries.PORTAL_TYPE, PortalCubed.id(secondaryType)), true, secondaryColor, true))
						.build()
		);
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
			entries.addAfter(Items.DEEPSLATE_IRON_ORE,
					PortalCubedBlocks.MAGNESIUM_ORE,
					PortalCubedBlocks.DEEPSLATE_MAGNESIUM_ORE
			);
			entries.addAfter(Items.RAW_IRON_BLOCK, PortalCubedBlocks.RAW_MAGNESIUM_BLOCK);
			entries.addAfter(Blocks.CHERRY_LOG, PortalCubedBlocks.LEMON_LOG);
			entries.addAfter(Blocks.CHERRY_LEAVES, PortalCubedBlocks.LEMON_LEAVES);
			entries.addAfter(Blocks.CHERRY_SAPLING, PortalCubedBlocks.LEMON_SAPLING);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.addAfter(Items.CHAIN, PortalCubedBlocks.MAGNESIUM_BLOCK);
			entries.addAfter(Blocks.CHERRY_BUTTON,
					PortalCubedBlocks.LEMON_LOG,
					PortalCubedBlocks.LEMON_WOOD,
					PortalCubedBlocks.STRIPPED_LEMON_LOG,
					PortalCubedBlocks.STRIPPED_LEMON_WOOD,
					PortalCubedBlocks.LEMON_PLANKS,
					PortalCubedBlocks.LEMON_STAIRS,
					PortalCubedBlocks.LEMON_SLAB,
					PortalCubedBlocks.LEMON_FENCE,
					PortalCubedBlocks.LEMON_FENCE_GATE,
					PortalCubedBlocks.LEMON_DOOR,
					PortalCubedBlocks.LEMON_TRAPDOOR,
					PortalCubedBlocks.LEMON_PRESSURE_PLATE,
					PortalCubedBlocks.LEMON_BUTTON
			);
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
			entries.addAfter(Blocks.CHERRY_HANGING_SIGN,
					PortalCubedItems.LEMON_SIGN,
					PortalCubedItems.LEMON_HANGING_SIGN
			);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.addAfter(Items.CHERRY_CHEST_BOAT, PortalCubedItems.LEMON_BOAT, PortalCubedItems.LEMON_CHEST_BOAT);
			entries.addAfter(Items.NETHERITE_HOE, PortalCubedItems.HAMMER, PortalCubedItems.CROWBAR);
			entries.addAfter(Items.MILK_BUCKET, PortalCubedItems.GOO_BUCKET);
			entries.addAfter(Items.WARPED_FUNGUS_ON_A_STICK, PortalCubedItems.CONSTRUCTION_CANNON);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.addAfter(Items.END_CRYSTAL, PortalCubedItems.LEMONADE);
			entries.addAfter(Items.TURTLE_HELMET,
					PortalCubedItems.ADVANCED_KNEE_REPLACEMENTS,
					PortalCubedItems.LONG_FALL_BOOTS
			);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
			if (!entries.shouldShowOpRestrictedItems())
				return;

			entries.addAfter(Items.BARRIER, PortalCubedBlocks.PROP_BARRIER, PortalCubedBlocks.PORTAL_BARRIER);
			entries.addAfter(Items.DEBUG_STICK, PortalCubedItems.FIZZLEINATOR);
		});
	}
}
