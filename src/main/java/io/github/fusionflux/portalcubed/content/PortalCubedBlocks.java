package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import java.util.EnumMap;
import java.util.Map;

import com.terraformersmc.terraform.sign.api.block.TerraformHangingSignBlock;
import com.terraformersmc.terraform.sign.api.block.TerraformSignBlock;
import com.terraformersmc.terraform.sign.api.block.TerraformWallHangingSignBlock;
import com.terraformersmc.terraform.sign.api.block.TerraformWallSignBlock;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.button.CubeButtonBlock;
import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.button.P1FloorButtonBlockItem;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import io.github.fusionflux.portalcubed.content.decoration.CrossbarBlock;
import io.github.fusionflux.portalcubed.content.decoration.CrossbarPillarBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlock;
import io.github.fusionflux.portalcubed.content.decoration.signage.small.SmallSignageBlock;
import io.github.fusionflux.portalcubed.content.door.ChamberDoorBlock;
import io.github.fusionflux.portalcubed.content.door.ChamberDoorMaterial;
import io.github.fusionflux.portalcubed.content.door.ChamberDoorType;
import io.github.fusionflux.portalcubed.content.goo.GooBlock;
import io.github.fusionflux.portalcubed.content.goo.GooCauldronBlock;
import io.github.fusionflux.portalcubed.content.misc.MagnesiumFireBlock;
import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed.content.prop.PropBarrierBlock;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.block.CollisionlessFacadeBlock;
import io.github.fusionflux.portalcubed.framework.block.FacadeBlock;
import io.github.fusionflux.portalcubed.content.portal.PortalBarrierBlock;
import io.github.fusionflux.portalcubed.framework.block.SaneStairBlock;
import io.github.fusionflux.portalcubed.framework.block.TransparentSlabBlock;
import io.github.fusionflux.portalcubed.framework.block.VerticalConnectiveDirectionalBlock;
import io.github.fusionflux.portalcubed.framework.block.cake.CakeBlockSet;
import io.github.fusionflux.portalcubed.framework.item.MultiBlockItem;
import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class PortalCubedBlocks {
	// ----- magnesium -----
	public static final Block MAGNESIUM_ORE = REGISTRAR.blocks.simple("magnesium_ore", Blocks.IRON_ORE);
	public static final Block DEEPSLATE_MAGNESIUM_ORE = REGISTRAR.blocks.simple("deepslate_magnesium_ore", Blocks.DEEPSLATE_IRON_ORE);
	public static final Block MAGNESIUM_BLOCK = REGISTRAR.blocks.createFrom("magnesium_block", Blocks.IRON_BLOCK)
			.properties(s -> s.mapColor(MapColor.CLAY))
			.build();
	public static final Block RAW_MAGNESIUM_BLOCK = REGISTRAR.blocks.createFrom("raw_magnesium_block", Blocks.RAW_IRON_BLOCK)
			.properties(s -> s.mapColor(MapColor.CLAY))
			.build();
	public static final Block MAGNESIUM_FIRE = REGISTRAR.blocks.createFrom("magnesium_fire", MagnesiumFireBlock::new, Blocks.SOUL_FIRE)
			.properties(s -> s.mapColor(MapColor.SNOW))
			.item(BlockItemProvider::noItem)
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- cake -----
	public static final CakeBlockSet BLACK_FOREST_CAKE = CakeBlockSet.builder("black_forest_cake", REGISTRAR)
			.all(builder -> builder.copyFrom(Blocks.CAKE))
			.base(builder -> builder.item((name, block, item) -> item.compostChance(1)))
			.build();
	// ----- floor buttons -----
	public static final FloorButtonBlock FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.createFrom("floor_button", FloorButtonBlock::new, Blocks.STONE)
			.item(P1FloorButtonBlockItem::new)
			.properties(s -> s.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock CUBE_BUTTON_BLOCK = REGISTRAR.blocks.createFrom("cube_button", CubeButtonBlock::new, Blocks.STONE)
			.item(MultiBlockItem::new)
			.properties(s -> s.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock OLD_AP_FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.createFrom("old_ap_floor_button", FloorButtonBlock::oldAp, Blocks.STONE)
			.item(MultiBlockItem::new)
			.properties(s -> s.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock PORTAL_1_FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.createFrom("portal_1_floor_button", FloorButtonBlock::p1, Blocks.STONE)
			.item(P1FloorButtonBlockItem::new)
			.properties(s -> s.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- pedestal buttons -----
	public static final PedestalButtonBlock PEDESTAL_BUTTON = REGISTRAR.blocks.createFrom("pedestal_button", PedestalButtonBlock::new, Blocks.STONE)
			.properties(s -> s.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final PedestalButtonBlock OLD_AP_PEDESTAL_BUTTON = REGISTRAR.blocks.createFrom("old_ap_pedestal_button", PedestalButtonBlock::oldAp, Blocks.STONE)
			.properties(s -> s.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- chamber doors -----
	public static final Map<ChamberDoorType, Map<ChamberDoorMaterial, ChamberDoorBlock>> CHAMBER_DOORS = Util.make(
			new EnumMap<>(ChamberDoorType.class),
			materials -> {
				for (ChamberDoorType type : ChamberDoorType.values()) {
					Map<ChamberDoorMaterial, ChamberDoorBlock> blocks = new EnumMap<>(ChamberDoorMaterial.class);
					materials.put(type, blocks);
					for (ChamberDoorMaterial material : type.materials) {
						String name = material.name + "_" + type.name;
						ChamberDoorBlock block = REGISTRAR.blocks.create(name, type::createBlock)
								.properties(material::makeProperties)
								.renderType(RenderTypes.CUTOUT)
								.build();
						blocks.put(material, block);
					}
				}
			}
	);
	// ----- panels -----
	public static final Map<PanelMaterial, Map<PanelPart, Block>> PANELS = Util.make(
			new EnumMap<>(PanelMaterial.class),
			materials -> {
				for (PanelMaterial material : PanelMaterial.values()) {
					Map<PanelPart, Block> blocks = new EnumMap<>(PanelPart.class);
					materials.put(material, blocks);

					Block base = REGISTRAR.blocks.create(material.name + "_panel")
							.properties(material::makeProperties)
							.build();
					blocks.put(PanelPart.SINGLE, base);

					for (PanelPart part : material.parts) {
						if (part == PanelPart.SINGLE)
							continue; // registered above

						String name = material.name + "_" + part.name;
						Block block = REGISTRAR.blocks.create(name, part::createBlock)
								.properties(material::makeProperties).build();
						blocks.put(part, block);
					}
				}
			});
	// ----- lemon -----
	public static final RotatedPillarBlock LEMON_LOG = REGISTRAR.blocks.create("lemon_log", RotatedPillarBlock::new)
			.copyFrom(Blocks.OAK_LOG)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_GRAY))
			.build();
	public static final RotatedPillarBlock STRIPPED_LEMON_LOG = REGISTRAR.blocks.create("stripped_lemon_log", RotatedPillarBlock::new)
			.copyFrom(Blocks.STRIPPED_OAK_LOG)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.strippedOf(LEMON_LOG)
			.build();
	public static final RotatedPillarBlock LEMON_WOOD = REGISTRAR.blocks.create("lemon_wood", RotatedPillarBlock::new)
			.copyFrom(Blocks.STRIPPED_OAK_WOOD)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_GRAY))
			.build();
	public static final RotatedPillarBlock STRIPPED_LEMON_WOOD = REGISTRAR.blocks.create("stripped_lemon_wood", RotatedPillarBlock::new)
			.copyFrom(Blocks.OAK_WOOD)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.strippedOf(LEMON_WOOD)
			.build();
	public static final LeavesBlock LEMON_LEAVES = REGISTRAR.blocks.create("lemon_leaves", LeavesBlock::new)
			.copyFrom(Blocks.OAK_LEAVES)
			.flammability(60, 30)
			.item((name, block, builder) -> builder.compostChance(0.3))
			.build();
	public static final SaplingBlock LEMON_SAPLING = REGISTRAR.blocks.create("lemon_sapling", settings -> new SaplingBlock(PortalCubedFeatures.LEMON_TREE_GROWER, settings))
			.copyFrom(Blocks.OAK_SAPLING)
			.renderType(RenderTypes.CUTOUT)
			.item((name, block, builder) -> builder.compostChance(0.3))
			.build();
	public static final FlowerPotBlock POTTED_LEMON_SAPLING = REGISTRAR.blocks.create("potted_lemon_sapling", settings -> new FlowerPotBlock(LEMON_SAPLING, settings))
			.copyFrom(Blocks.POTTED_OAK_SAPLING)
			.item(BlockItemProvider::noItem)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final Block LEMON_PLANKS = REGISTRAR.blocks.create("lemon_planks", Block::new)
			.copyFrom(Blocks.OAK_PLANKS)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.flammability(20, 5)
			.build();
	public static final SlabBlock LEMON_SLAB = REGISTRAR.blocks.create("lemon_slab", SlabBlock::new)
			.copyFrom(Blocks.OAK_SLAB)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.flammability(20, 5)
			.build();
	public static final SaneStairBlock LEMON_STAIRS = REGISTRAR.blocks.create("lemon_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.OAK_STAIRS)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.flammability(20, 5)
			.build();
	public static final FenceBlock LEMON_FENCE = REGISTRAR.blocks.create("lemon_fence", FenceBlock::new)
			.copyFrom(Blocks.OAK_FENCE)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.flammability(20, 5)
			.build();
	public static final FenceGateBlock LEMON_FENCE_GATE = REGISTRAR.blocks.create("lemon_fence_gate", properties -> new FenceGateBlock(WoodType.OAK, properties))
			.copyFrom(Blocks.OAK_FENCE_GATE)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.flammability(20, 5)
			.build();
	public static final ButtonBlock LEMON_BUTTON = REGISTRAR.blocks.create("lemon_button", properties -> new ButtonBlock(BlockSetType.OAK, 30, properties))
			.copyFrom(Blocks.OAK_BUTTON)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.build();
	public static final PressurePlateBlock LEMON_PRESSURE_PLATE = REGISTRAR.blocks.create("lemon_pressure_plate", properties -> new PressurePlateBlock(BlockSetType.OAK, properties))
			.copyFrom(Blocks.OAK_PRESSURE_PLATE)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.build();
	public static final ResourceLocation LEMON_SIGN_TEXTURE = PortalCubed.id("entity/signs/lemon");
	public static final TerraformSignBlock LEMON_SIGN = REGISTRAR.blocks.create("lemon_sign", properties -> new TerraformSignBlock(LEMON_SIGN_TEXTURE, properties))
			.copyFrom(Blocks.OAK_SIGN)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_YELLOW)
			)
			.item(BlockItemProvider::noItem)
			.build();
	public static final TerraformWallSignBlock LEMON_WALL_SIGN = REGISTRAR.blocks.create("lemon_wall_sign", properties -> new TerraformWallSignBlock(LEMON_SIGN_TEXTURE, properties))
			.copyFrom(Blocks.OAK_WALL_SIGN)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.overrideLootTable(LEMON_SIGN.getLootTable())
			)
			.item(BlockItemProvider::noItem)
			.build();
	public static final ResourceLocation LEMON_HANGING_SIGN_TEXTURE = PortalCubed.id("entity/signs/hanging/lemon");
	public static final ResourceLocation LEMON_HANGING_SIGN_GUI_TEXTURE = PortalCubed.id("textures/gui/hanging_signs/lemon");
	public static final TerraformHangingSignBlock LEMON_HANGING_SIGN = REGISTRAR.blocks.create("lemon_hanging_sign", properties -> new TerraformHangingSignBlock(LEMON_HANGING_SIGN_TEXTURE, LEMON_HANGING_SIGN_GUI_TEXTURE, properties))
			.copyFrom(Blocks.OAK_HANGING_SIGN)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.item(BlockItemProvider::noItem)
			.build();
	public static final TerraformWallHangingSignBlock LEMON_WALL_HANGING_SIGN = REGISTRAR.blocks.create("lemon_wall_hanging_sign", properties -> new TerraformWallHangingSignBlock(LEMON_HANGING_SIGN_TEXTURE, LEMON_HANGING_SIGN_GUI_TEXTURE, properties))
			.copyFrom(Blocks.OAK_WALL_HANGING_SIGN)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.overrideLootTable(LEMON_HANGING_SIGN.getLootTable())
			)
			.item(BlockItemProvider::noItem)
			.build();
	public static final DoorBlock LEMON_DOOR = REGISTRAR.blocks.create("lemon_door", properties -> new DoorBlock(BlockSetType.OAK, properties))
			.copyFrom(Blocks.OAK_DOOR)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final TrapDoorBlock LEMON_TRAPDOOR = REGISTRAR.blocks.create("lemon_trapdoor", properties -> new TrapDoorBlock(BlockSetType.OAK, properties))
			.copyFrom(Blocks.OAK_TRAPDOOR)
			.properties(settings -> settings.mapColor(MapColor.TERRACOTTA_YELLOW))
			.renderType(RenderTypes.CUTOUT)
			.build();

	// ----- signage -----
	public static final LargeSignageBlock LARGE_SIGNAGE = REGISTRAR.blocks.create("large_signage", LargeSignageBlock::new)
			.properties(settings -> settings
					.instrument(NoteBlockInstrument.HAT)
					.strength(0.3F)
					.mapColor(MapColor.QUARTZ)
					.sound(SoundType.COPPER_BULB)
			)
			.build();
	public static final LargeSignageBlock AGED_LARGE_SIGNAGE = REGISTRAR.blocks.createFrom("aged_large_signage", LargeSignageBlock::new, LARGE_SIGNAGE)
			.properties(settings -> settings.mapColor(MapColor.SAND))
			.build();
	public static final SmallSignageBlock SMALL_SIGNAGE = REGISTRAR.blocks.createFrom("small_signage", SmallSignageBlock::new, LARGE_SIGNAGE)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.build();
	public static final SmallSignageBlock AGED_SMALL_SIGNAGE = REGISTRAR.blocks.createFrom("aged_small_signage", SmallSignageBlock::new, SMALL_SIGNAGE)
			.properties(settings -> settings.mapColor(MapColor.SAND))
			.build();

	// ----- misc blocks - tiles -----
	public static final Block PORTAL_1_METAL_TILES = REGISTRAR.blocks.create("portal_1_metal_tiles", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.sound(SoundType.STONE)
			)
			.build();
	public static final SlabBlock PORTAL_1_METAL_TILE_SLAB = REGISTRAR.blocks.create("portal_1_metal_tile_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.sound(SoundType.STONE)
			)
			.build();
	public static final SaneStairBlock PORTAL_1_METAL_TILE_STAIRS = REGISTRAR.blocks.create("portal_1_metal_tile_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.sound(SoundType.STONE)
			)
			.build();
	public static final CollisionlessFacadeBlock PORTAL_1_METAL_TILE_FACADE = REGISTRAR.blocks.create("portal_1_metal_tile_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.sound(SoundType.STONE)
			)
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
	public static final CollisionlessFacadeBlock LARGE_BLUE_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("large_blue_office_tile_facade", CollisionlessFacadeBlock::new)
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
	public static final CollisionlessFacadeBlock SMALL_BLUE_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("small_blue_office_tile_facade", CollisionlessFacadeBlock::new)
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
	public static final CollisionlessFacadeBlock BLACK_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("black_office_tile_facade", CollisionlessFacadeBlock::new)
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
	public static final CollisionlessFacadeBlock GRAY_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("gray_office_tile_facade", CollisionlessFacadeBlock::new)
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
	public static final CollisionlessFacadeBlock BROWN_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("brown_office_tile_facade", CollisionlessFacadeBlock::new)
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
	public static final CollisionlessFacadeBlock ORANGE_OFFICE_TILE_FACADE = REGISTRAR.blocks.create("orange_office_tile_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	// ----- misc blocks - office concrete -----
	public static final Block OFFICE_CONCRETE = REGISTRAR.blocks.create("office_concrete", Block::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final SlabBlock OFFICE_CONCRETE_SLAB = REGISTRAR.blocks.create("office_concrete_slab", SlabBlock::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final CollisionlessFacadeBlock OFFICE_CONCRETE_FACADE = REGISTRAR.blocks.create("office_concrete_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final Block BLUE_OFFICE_CONCRETE = REGISTRAR.blocks.create("blue_office_concrete", Block::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final SlabBlock BLUE_OFFICE_CONCRETE_SLAB = REGISTRAR.blocks.create("blue_office_concrete_slab", SlabBlock::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final CollisionlessFacadeBlock BLUE_OFFICE_CONCRETE_FACADE = REGISTRAR.blocks.create("blue_office_concrete_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final Block STRIPED_OFFICE_CONCRETE = REGISTRAR.blocks.create("striped_office_concrete", Block::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final SlabBlock STRIPED_OFFICE_CONCRETE_SLAB = REGISTRAR.blocks.create("striped_office_concrete_slab", SlabBlock::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final CollisionlessFacadeBlock STRIPED_OFFICE_CONCRETE_FACADE = REGISTRAR.blocks.create("striped_office_concrete_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.WHITE_TERRACOTTA)
			.build();
	public static final Block WHITE_OFFICE_CONCRETE = REGISTRAR.blocks.create("white_office_concrete", Block::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.build();
	public static final SlabBlock WHITE_OFFICE_CONCRETE_SLAB = REGISTRAR.blocks.create("white_office_concrete_slab", SlabBlock::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.build();
	public static final SaneStairBlock WHITE_OFFICE_CONCRETE_STAIRS = REGISTRAR.blocks.create("white_office_concrete_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.build();
	public static final WallBlock WHITE_OFFICE_CONCRETE_WALL = REGISTRAR.blocks.create("white_office_concrete_wall", WallBlock::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.build();
	public static final CollisionlessFacadeBlock WHITE_OFFICE_CONCRETE_FACADE = REGISTRAR.blocks.create("white_office_concrete_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.build();
	public static final Block LIGHT_GRAY_OFFICE_CONCRETE = REGISTRAR.blocks.create("light_gray_office_concrete", Block::new)
			.copyFrom(Blocks.LIGHT_GRAY_CONCRETE)
			.build();
	public static final SlabBlock LIGHT_GRAY_OFFICE_CONCRETE_SLAB = REGISTRAR.blocks.create("light_gray_office_concrete_slab", SlabBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_CONCRETE)
			.build();
	public static final SaneStairBlock LIGHT_GRAY_OFFICE_CONCRETE_STAIRS = REGISTRAR.blocks.create("light_gray_office_concrete_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_CONCRETE)
			.build();
	public static final WallBlock LIGHT_GRAY_OFFICE_CONCRETE_WALL = REGISTRAR.blocks.create("light_gray_office_concrete_wall", WallBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_CONCRETE)
			.build();
	public static final CollisionlessFacadeBlock LIGHT_GRAY_OFFICE_CONCRETE_FACADE = REGISTRAR.blocks.create("light_gray_office_concrete_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.LIGHT_GRAY_CONCRETE)
			.build();
	public static final VerticalConnectiveDirectionalBlock VERTICAL_OFFICE_CONCRETE = REGISTRAR.blocks.create("vertical_office_concrete", VerticalConnectiveDirectionalBlock::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.build();
	// ----- misc blocks - elevator_walls -----
	public static final Block ELEVATOR_WALL_MIDDLE = REGISTRAR.blocks.create("elevator_wall_middle", Block::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.properties(settings -> settings.mapColor(MapColor.CLAY))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final VerticalConnectiveDirectionalBlock ELEVATOR_WALL_END = REGISTRAR.blocks.create("elevator_wall_end", VerticalConnectiveDirectionalBlock::new)
			.copyFrom(Blocks.WHITE_CONCRETE)
			.properties(settings -> settings.mapColor(MapColor.CLAY))
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- misc blocks - grates -----
	public static final WaterloggedTransparentBlock METAL_GRATE = REGISTRAR.blocks.create("metal_grate", WaterloggedTransparentBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final SlabBlock METAL_GRATE_SLAB = REGISTRAR.blocks.create("metal_grate_slab", TransparentSlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FacadeBlock METAL_GRATE_FACADE = REGISTRAR.blocks.create("metal_grate_facade", FacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.COLOR_GRAY)
					.pushReaction(PushReaction.DESTROY)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final WaterloggedTransparentBlock OLD_AP_METAL_GRATE = REGISTRAR.blocks.create("old_ap_metal_grate", WaterloggedTransparentBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final SlabBlock OLD_AP_METAL_GRATE_SLAB = REGISTRAR.blocks.create("old_ap_metal_grate_slab", TransparentSlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FacadeBlock OLD_AP_METAL_GRATE_FACADE = REGISTRAR.blocks.create("old_ap_metal_grate_facade", FacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final WaterloggedTransparentBlock PORTAL_1_METAL_GRATE = REGISTRAR.blocks.create("portal_1_metal_grate", WaterloggedTransparentBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.SAND)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final SlabBlock PORTAL_1_METAL_GRATE_SLAB = REGISTRAR.blocks.create("portal_1_metal_grate_slab", TransparentSlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.SAND)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FacadeBlock PORTAL_1_METAL_GRATE_FACADE = REGISTRAR.blocks.create("portal_1_metal_grate_facade", FacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.COPPER_GRATE)
					.mapColor(MapColor.SAND)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final WaterloggedTransparentBlock MESH_GRATE = REGISTRAR.blocks.create("mesh_grate", WaterloggedTransparentBlock::new)
			.copyFrom(Blocks.BLACK_WOOL)
			.properties(settings -> settings
					.sound(SoundType.VINE)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final SlabBlock MESH_GRATE_SLAB = REGISTRAR.blocks.create("mesh_grate_slab", TransparentSlabBlock::new)
			.copyFrom(Blocks.BLACK_WOOL)
			.properties(settings -> settings
					.sound(SoundType.VINE)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FacadeBlock MESH_GRATE_FACADE = REGISTRAR.blocks.create("mesh_grate_facade", FacadeBlock::new)
			.copyFrom(Blocks.BLACK_WOOL)
			.properties(settings -> settings
					.sound(SoundType.VINE)
					.noOcclusion()
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- misc blocks - crossbars -----
	public static final CrossbarPillarBlock CROSSBAR_PILLAR = REGISTRAR.blocks.create("crossbar_pillar", CrossbarPillarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock DOUBLE_2x2_CROSSBAR_TOP_LEFT = REGISTRAR.blocks.create("double_2x2_crossbar_top_left", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock DOUBLE_2x2_CROSSBAR_TOP_RIGHT = REGISTRAR.blocks.create("double_2x2_crossbar_top_right", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock DOUBLE_2x2_CROSSBAR_BOTTOM_LEFT = REGISTRAR.blocks.create("double_2x2_crossbar_bottom_left", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock DOUBLE_2x2_CROSSBAR_BOTTOM_RIGHT = REGISTRAR.blocks.create("double_2x2_crossbar_bottom_right", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock SINGLE_2x2_CROSSBAR_TOP_LEFT = REGISTRAR.blocks.create("2x2_crossbar_top_left", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock SINGLE_2x2_CROSSBAR_TOP_RIGHT = REGISTRAR.blocks.create("2x2_crossbar_top_right", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock SINGLE_2x2_CROSSBAR_BOTTOM_LEFT = REGISTRAR.blocks.create("2x2_crossbar_bottom_left", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final CrossbarBlock SINGLE_2x2_CROSSBAR_BOTTOM_RIGHT = REGISTRAR.blocks.create("2x2_crossbar_bottom_right", CrossbarBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.sound(SoundType.LANTERN)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion()
					.isSuffocating(Blocks::never)
					.isViewBlocking(Blocks::never)
			)
			.renderType(RenderTypes.CUTOUT)
			.build();
	// ----- misc blocks - metal plating -----
	public static final Block METAL_PLATING = REGISTRAR.blocks.create("metal_plating", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SlabBlock METAL_PLATING_SLAB = REGISTRAR.blocks.create("metal_plating_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SaneStairBlock METAL_PLATING_STAIRS = REGISTRAR.blocks.create("metal_plating_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final CollisionlessFacadeBlock METAL_PLATING_FACADE = REGISTRAR.blocks.create("metal_plating_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final Block CUT_METAL_PLATING = REGISTRAR.blocks.create("cut_metal_plating", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SlabBlock CUT_METAL_PLATING_SLAB = REGISTRAR.blocks.create("cut_metal_plating_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final Block TREAD_PLATE = REGISTRAR.blocks.create("tread_plate", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.COLOR_LIGHT_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SlabBlock TREAD_PLATE_SLAB = REGISTRAR.blocks.create("tread_plate_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.COLOR_LIGHT_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final CollisionlessFacadeBlock TREAD_PLATE_FACADE = REGISTRAR.blocks.create("tread_plate_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.COLOR_LIGHT_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final Block DIRTY_TREAD_PLATE = REGISTRAR.blocks.create("dirty_tread_plate", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.COLOR_LIGHT_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final Block DIRTY_METAL_PLATING = REGISTRAR.blocks.create("dirty_metal_plating", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SlabBlock DIRTY_METAL_PLATING_SLAB = REGISTRAR.blocks.create("dirty_metal_plating_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SaneStairBlock DIRTY_METAL_PLATING_STAIRS = REGISTRAR.blocks.create("dirty_metal_plating_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final CollisionlessFacadeBlock DIRTY_METAL_PLATING_FACADE = REGISTRAR.blocks.create("dirty_metal_plating_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final Block DIRTY_CUT_METAL_PLATING = REGISTRAR.blocks.create("dirty_cut_metal_plating", Block::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SlabBlock DIRTY_CUT_METAL_PLATING_SLAB = REGISTRAR.blocks.create("dirty_cut_metal_plating_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final SlabBlock DIRTY_TREAD_PLATE_SLAB = REGISTRAR.blocks.create("dirty_tread_plate_slab", SlabBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	public static final CollisionlessFacadeBlock DIRTY_TREAD_PLATE_FACADE = REGISTRAR.blocks.create("dirty_tread_plate_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.COPPER_BLOCK)
			.properties(settings -> settings
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
			)
			.build();
	// ----- misc blocks - chamber exteriors -----
	public static final Block GRAY_CHAMBER_EXTERIOR = REGISTRAR.blocks.create("gray_chamber_exterior", Block::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final CollisionlessFacadeBlock GRAY_CHAMBER_EXTERIOR_FACADE = REGISTRAR.blocks.create("gray_chamber_exterior_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_A_TOP_LEFT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_a_top_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_A_TOP_RIGHT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_a_top_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_A_BOTTOM_LEFT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_a_bottom_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_A_BOTTOM_RIGHT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_a_bottom_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_B_TOP_LEFT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_b_top_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_B_TOP_RIGHT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_b_top_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_B_BOTTOM_LEFT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_b_bottom_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final GlazedTerracottaBlock GRAY_2x2_CHAMBER_EXTERIOR_B_BOTTOM_RIGHT = REGISTRAR.blocks.create("gray_2x2_chamber_exterior_b_bottom_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.GRAY_CONCRETE)
			.build();
	public static final Block YELLOW_CHAMBER_EXTERIOR = REGISTRAR.blocks.create("yellow_chamber_exterior", Block::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final CollisionlessFacadeBlock YELLOW_CHAMBER_EXTERIOR_FACADE = REGISTRAR.blocks.create("yellow_chamber_exterior_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_A_TOP_LEFT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_a_top_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_A_TOP_RIGHT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_a_top_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_A_BOTTOM_LEFT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_a_bottom_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_A_BOTTOM_RIGHT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_a_bottom_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_B_TOP_LEFT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_b_top_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_B_TOP_RIGHT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_b_top_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_B_BOTTOM_LEFT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_b_bottom_left", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	public static final GlazedTerracottaBlock YELLOW_2x2_CHAMBER_EXTERIOR_B_BOTTOM_RIGHT = REGISTRAR.blocks.create("yellow_2x2_chamber_exterior_b_bottom_right", GlazedTerracottaBlock::new)
			.copyFrom(Blocks.YELLOW_TERRACOTTA)
			.build();
	// ----- misc blocks - random -----
	public static final Block INSULATION = REGISTRAR.blocks.create("insulation", Block::new)
			.copyFrom(Blocks.YELLOW_WOOL)
			.build();
	public static final CollisionlessFacadeBlock INSULATION_FACADE = REGISTRAR.blocks.create("insulation_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.YELLOW_WOOL)
			.build();
	public static final Block PLYWOOD = REGISTRAR.blocks.create("plywood", Block::new)
			.copyFrom(Blocks.OAK_PLANKS)
			.build();
	public static final SlabBlock PLYWOOD_SLAB = REGISTRAR.blocks.create("plywood_slab", SlabBlock::new)
			.copyFrom(Blocks.OAK_PLANKS)
			.build();
	public static final SaneStairBlock PLYWOOD_STAIRS = REGISTRAR.blocks.create("plywood_stairs", SaneStairBlock::new)
			.copyFrom(Blocks.OAK_PLANKS)
			.build();
	public static final WallBlock PLYWOOD_WALL = REGISTRAR.blocks.create("plywood_wall", WallBlock::new)
			.copyFrom(Blocks.OAK_PLANKS)
			.build();
	public static final CollisionlessFacadeBlock PLYWOOD_FACADE = REGISTRAR.blocks.create("plywood_facade", CollisionlessFacadeBlock::new)
			.copyFrom(Blocks.OAK_PLANKS)
			.build();

	public static final Block SEWAGE = REGISTRAR.blocks.createFrom("sewage", Blocks.MUD)
			.properties(s -> s
					.mapColor(MapColor.COLOR_BROWN)
					.friction(0.9f)
					.jumpFactor(0.05f)
					.sound(new SoundType(
						1.0F, 1.0F,
						SoundEvents.MUD_BREAK,
						PortalCubedSounds.SEWAGE_STEP,
						SoundEvents.MUD_PLACE,
						SoundEvents.MUD_HIT,
						SoundEvents.MUD_FALL)
					)
			)
			.build();

	public static final PropBarrierBlock PROP_BARRIER = REGISTRAR.blocks.createFrom("prop_barrier", PropBarrierBlock::new, Blocks.BARRIER)
			.properties(BlockBehaviour.Properties::dynamicShape)
			.item((block, properties) -> new BlockItem(block, properties.rarity(Rarity.EPIC)))
			.build();
	public static final PortalBarrierBlock PORTAL_BARRIER = REGISTRAR.blocks.createFrom("portal_barrier", PortalBarrierBlock::new, Blocks.BARRIER)
			.properties(BlockBehaviour.Properties::dynamicShape)
			//.item((block, properties) -> new BlockItem(block, properties.rarity(Rarity.EPIC)))
			.build();

	public static final Block GOO = REGISTRAR.blocks.createFrom("toxic_goo", GooBlock::new, Blocks.WATER)
			.properties(s -> s.mapColor(MapColor.TERRACOTTA_GREEN))
			.item(BlockItemProvider::noItem)
			.build();

	public static final Block GOO_CAULDRON = REGISTRAR.blocks.createFrom("toxic_goo_cauldron", GooCauldronBlock::new, Blocks.CAULDRON)
			.item(BlockItemProvider::noItem)
			.build();

	public static void init() {
		CauldronFluidContent.registerCauldron(GOO_CAULDRON, PortalCubedFluids.GOO, FluidConstants.BUCKET, null);
		FlammableBlockRegistry registry = FlammableBlockRegistry.getDefaultInstance();
		registry.add(PortalCubedBlockTags.MAGNESIUM_FIRE_BASE_BLOCKS, 1, 20);
		registry.add(PortalCubedBlockTags.LEMON_LOGS, 5, 5);
	}
}
