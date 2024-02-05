package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.content.button.OldApFloorButtonBlock;
import io.github.fusionflux.portalcubed.content.button.P1FloorButtonBlock;
import io.github.fusionflux.portalcubed.framework.item.MultiBlockItem;
import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;

import java.util.EnumMap;
import java.util.Map;

public class PortalCubedBlocks {
	public static final RotatedPillarBlock TEST_BLOCK = REGISTRAR.blocks.create("test_block", RotatedPillarBlock::new)
			.copyFrom(Blocks.STONE)
			.settings(QuiltBlockSettings::noCollision)
			.item(BlockItemProvider::noItem)
			.build();

	public static final FloorButtonBlock FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("floor_button", FloorButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.item((block, properties) -> new MultiBlockItem(block, properties))
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock OLD_AP_FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("old_ap_floor_button", OldApFloorButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.item((block, properties) -> new MultiBlockItem(block, properties))
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();
	public static final FloorButtonBlock PORTAL_1_FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("portal_1_floor_button", P1FloorButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.item((block, properties) -> new MultiBlockItem(block, properties))
			.settings(settings -> settings.pushReaction(PushReaction.BLOCK).mapColor(MapColor.TERRACOTTA_RED))
			.renderType(RenderTypes.CUTOUT)
			.build();

	public static final Map<PanelMaterial, Map<PanelPart, Block>> PANELS = Util.make(
			new EnumMap<>(PanelMaterial.class),
			materials -> {
				for (PanelMaterial material : PanelMaterial.values()) {
					Map<PanelPart, Block> blocks = new EnumMap<>(PanelPart.class);
					materials.put(material, blocks);

					Block base = REGISTRAR.blocks.create(material.name + "_panel")
							.settings(material.getSettings())
							.build();

					for (PanelPart part : material.parts) {
						if (part == PanelPart.SINGLE)
							continue;

						String name = material.name + "_" + part.name;
						Block block = REGISTRAR.blocks.create(name, part::createBlock)
								.settings(material.getSettings())
								.settings(s -> {
									// most panels will just drop the main one.
									// Half is an exception
									if (part != PanelPart.HALF) {
										s.dropsLike(base);
									}
                                })
								.build();
						blocks.put(part, block);
					}
				}
			});

	public static void init() {
	}
}
