package io.github.fusionflux.portalcubed.content.panel;

import io.github.fusionflux.portalcubed.framework.block.NoCollisionMultifaceBlock;
import io.github.fusionflux.portalcubed.framework.block.ConnectiveDirectionalBlock;
import io.github.fusionflux.portalcubed.framework.block.SaneStairBlock;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

public enum PanelPart {
	CHECKERED("checkered_panel"), // white only
	CHECKERED_SLAB("checkered_panel_slab", SlabBlock::new),
	CHECKERED_STAIRS("checkered_panel_stairs", SaneStairBlock::new),
	CHECKERED_FACADE("checkered_panel_facade", NoCollisionMultifaceBlock::new),
	HALF("half_panel"),
	HALF_SLAB("half_panel_slab", SlabBlock::new),
	HALF_STAIRS("half_panel_stairs", SaneStairBlock::new),
	HALF_FACADE("half_panel_facade", NoCollisionMultifaceBlock::new),
	SINGLE("panel"),
	SLAB("panel_slab", SlabBlock::new),
	STAIRS("panel_stairs", SaneStairBlock::new),
	FACADE("panel_facade", NoCollisionMultifaceBlock::new),
	MULTI_1x2("2x1_panel", ConnectiveDirectionalBlock::new),
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
