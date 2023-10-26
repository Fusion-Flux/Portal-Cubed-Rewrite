package io.github.fusionflux.portalcubed.content;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedBlocks {
	public static final RotatedPillarBlock TEST_BLOCK = REGISTRAR.blocks.create("test_block", RotatedPillarBlock::new)
			.copyFrom(Blocks.STONE)
			.settings(QuiltBlockSettings::noCollision)
			.itemSettings(QuiltItemSettings::fireproof)
			.build();

	public static void init() {
	}
}
