package io.github.fusionflux.portalcubed.content.panel;

import io.github.fusionflux.portalcubed.framework.registration.block.BlockFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

public enum PanelPart {
	CHECKERED("checkered_panel"), // white only
	HALF("half_panel"),
	SINGLE("panel"),
	MULTI_1x2_BOTTOM("2x1_panel_bottom", RotatedPillarBlock::new),
	MULTI_1x2_TOP("2x1_panel_top", RotatedPillarBlock::new),
	JOINER("2x1_panel_joiner", RotatedPillarBlock::new), // portal 1 metal only
	MULTI_2x2_BOTTOM_LEFT("2x2_panel_bottom_left", GlazedTerracottaBlock::new),
	MULTI_2x2_BOTTOM_RIGHT("2x2_panel_bottom_right", GlazedTerracottaBlock::new),
	MULTI_2x2_TOP_LEFT("2x2_panel_top_left", GlazedTerracottaBlock::new),
	MULTI_2x2_TOP_RIGHT("2x2_panel_top_right", GlazedTerracottaBlock::new);

	public final String name;
	private final BlockFactory<Block> factory;

	PanelPart(String name) {
		this(name, Block::new);
	}

	PanelPart(String name, BlockFactory<Block> factory) {
		this.name = name;
		this.factory = factory;
	}

	public Block createBlock(QuiltBlockSettings settings) {
		return this.factory.create(settings);
	}
}
