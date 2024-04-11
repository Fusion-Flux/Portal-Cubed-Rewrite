package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed.framework.block.SaneStairBlock;
import io.github.fusionflux.portalcubed.framework.block.SimpleMultifaceBlock;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.CubeButtonBlock;
import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.framework.block.cake.CakeBlockSet;
import io.github.fusionflux.portalcubed.framework.item.MultiBlockItem;
import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;

import java.util.EnumMap;
import java.util.Map;

public class PortalCubedBlocks {
	// ----- magnesium -----
	public static final Block MAGNESIUM_ORE = REGISTRAR.blocks.create("magnesium_ore", Block::new)
			.copyFrom(Blocks.IRON_ORE)
			.build();
	public static final Block DEEPSLATE_MAGNESIUM_ORE = REGISTRAR.blocks.create("deepslate_magnesium_ore", Block::new)
			.copyFrom(Blocks.DEEPSLATE_IRON_ORE)
			.build();
	public static final Block MAGNESIUM_BLOCK = REGISTRAR.blocks.create("magnesium_block", Block::new)
			.copyFrom(Blocks.IRON_BLOCK)
			.settings(settings -> settings.mapColor(MapColor.CLAY))
			.build();
	public static final Block RAW_MAGNESIUM_BLOCK = REGISTRAR.blocks.create("raw_magnesium_block", Block::new)
			.copyFrom(Blocks.RAW_IRON_BLOCK)
			.settings(settings -> settings.mapColor(MapColor.CLAY))
			.build();
	// ----- cake -----
	public static final CakeBlockSet BLACK_FOREST_CAKE = new CakeBlockSet(
			"black_forest_cake", REGISTRAR, QuiltBlockSettings.copyOf(Blocks.CAKE)
	);
	// ----- floor buttons -----
	public static final FloorButtonBlock FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("floor_button", FloorButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.item(MultiBlockItem::new)
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock CUBE_BUTTON_BLOCK = REGISTRAR.blocks.create("cube_button", CubeButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.item(MultiBlockItem::new)
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock OLD_AP_FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("old_ap_floor_button", FloorButtonBlock::oldAp)
			.copyFrom(Blocks.STONE)
			.item(MultiBlockItem::new)
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock PORTAL_1_FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("portal_1_floor_button", FloorButtonBlock::p1)
			.copyFrom(Blocks.STONE)
			.item(MultiBlockItem::new)
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- pedestal buttons -----
	public static final PedestalButtonBlock PEDESTAL_BUTTON = REGISTRAR.blocks.create("pedestal_button", PedestalButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final PedestalButtonBlock OLD_AP_PEDESTAL_BUTTON = REGISTRAR.blocks.create("old_ap_pedestal_button", PedestalButtonBlock::oldAp)
			.copyFrom(Blocks.STONE)
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- panels -----
	public static final Map<PanelMaterial, Map<PanelPart, Block>> PANELS = Util.make(
			new EnumMap<>(PanelMaterial.class),
			materials -> {
				for (PanelMaterial material : PanelMaterial.values()) {
					Map<PanelPart, Block> blocks = new EnumMap<>(PanelPart.class);
					materials.put(material, blocks);

					Block base = REGISTRAR.blocks.create(material.name + "_panel")
							.settings(material.getSettings())
							.build();
					blocks.put(PanelPart.SINGLE, base);

					for (PanelPart part : material.parts) {
						if (part == PanelPart.SINGLE)
							continue; // registered above

						String name = material.name + "_" + part.name;
						Block block = REGISTRAR.blocks.create(name, part::createBlock)
								.settings(material.getSettings()).build();
						blocks.put(part, block);
					}
				}
			});
	// ----- misc blocks -----
	public static final Block PORTAL_1_METAL_TILES = REGISTRAR.blocks.create("portal_1_metal_tiles", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.settings(settings -> settings.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
			.build();
	public static final SlabBlock PORTAL_1_METAL_TILE_SLAB = REGISTRAR.blocks.create("portal_1_metal_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.build();
	public static final SaneStairBlock PORTAL_1_METAL_TILE_STAIRS = REGISTRAR.blocks.create("portal_1_metal_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.build();
	public static final SimpleMultifaceBlock PORTAL_1_METAL_TILE_FACADE = REGISTRAR.blocks.create("portal_1_metal_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.build();
	public static final Block LARGE_BLUE_OFFICE_TILES = REGISTRAR.blocks.create("large_blue_office_tiles", Block::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final SlabBlock LARGE_BLUE_OFFICE_TILE_SLAB = REGISTRAR.blocks.create("large_blue_office_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final SaneStairBlock LARGE_BLUE_OFFICE_TILE_STAIRS = REGISTRAR.blocks.create("large_blue_office_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final SimpleMultifaceBlock LARGE_BLUE_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("large_blue_office_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final Block SMALL_BLUE_OFFICE_TILES = REGISTRAR.blocks.create("small_blue_office_tiles", Block::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final SlabBlock SMALL_BLUE_OFFICE_TILE_SLAB = REGISTRAR.blocks.create("small_blue_office_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final SaneStairBlock SMALL_BLUE_OFFICE_TILE_STAIRS = REGISTRAR.blocks.create("small_blue_office_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final SimpleMultifaceBlock SMALL_BLUE_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("small_blue_office_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.CYAN_TERRACOTTA)
			.build();
	public static final Block BLACK_OFFICE_TILES = REGISTRAR.blocks.create("black_office_tiles", Block::new)
			.copyFrom(Blocks.BLACK_TERRACOTTA)
			.build();
	public static final SlabBlock BLACK_OFFICE_TILE_SLAB = REGISTRAR.blocks.create("black_office_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.BLACK_TERRACOTTA)
			.build();
	public static final SaneStairBlock BLACK_OFFICE_TILE_STAIRS = REGISTRAR.blocks.create("black_office_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.BLACK_TERRACOTTA)
			.build();
	public static final SimpleMultifaceBlock BLACK_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("black_office_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.BLACK_TERRACOTTA)
			.build();
	public static final Block GRAY_OFFICE_TILES = REGISTRAR.blocks.create("gray_office_tiles", Block::new)
			.copyFrom(Blocks.LIGHT_GRAY_TERRACOTTA)
			.build();
	public static final SlabBlock GRAY_OFFICE_TILE_SLAB = REGISTRAR.blocks.create("gray_office_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_TERRACOTTA)
			.build();
	public static final SaneStairBlock GRAY_OFFICE_TILE_STAIRS = REGISTRAR.blocks.create("gray_office_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_TERRACOTTA)
			.build();
	public static final SimpleMultifaceBlock GRAY_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("gray_office_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_TERRACOTTA)
			.build();
	public static final Block BROWN_OFFICE_TILES = REGISTRAR.blocks.create("brown_office_tiles", Block::new)
			.copyFrom(Blocks.BROWN_TERRACOTTA)
			.build();
	public static final SlabBlock BROWN_OFFICE_TILE_SLAB = REGISTRAR.blocks.create("brown_office_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.BROWN_TERRACOTTA)
			.build();
	public static final SaneStairBlock BROWN_OFFICE_TILE_STAIRS = REGISTRAR.blocks.create("brown_office_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.BROWN_TERRACOTTA)
			.build();
	public static final SimpleMultifaceBlock BROWN_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("brown_office_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.BROWN_TERRACOTTA)
			.build();
	public static final Block ORANGE_OFFICE_TILES = REGISTRAR.blocks.create("orange_office_tiles", Block::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final SlabBlock ORANGE_OFFICE_TILE_SLAB = REGISTRAR.blocks.create("orange_office_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final SaneStairBlock ORANGE_OFFICE_TILE_STAIRS = REGISTRAR.blocks.create("orange_office_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final SimpleMultifaceBlock ORANGE_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("orange_office_tile_facade", SimpleMultifaceBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();

	public static void init() {
	}
}
