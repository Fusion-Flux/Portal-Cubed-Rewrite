package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.test.TestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;

public class PortalCubedBlocks {
	public static final TestBlock TEST_BLOCK = REGISTRAR.blocks.create("test_block", TestBlock::new)
			.copyFrom(Blocks.STONE)
			.settings(QuiltBlockSettings::dynamicBounds)
			.item(BlockItemProvider::noItem)
			.build();

	public static void init() {
	}
}
