package io.github.fusionflux.portalcubed.content;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

	public static void init() {
	}
}
