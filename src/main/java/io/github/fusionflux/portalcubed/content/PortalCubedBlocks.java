package io.github.fusionflux.portalcubed.content;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.framework.block.AbstractMultiBlock;
import io.github.fusionflux.portalcubed.framework.item.MultiBlockItem;
import io.github.fusionflux.portalcubed.framework.registration.block.BlockItemProvider;

public class PortalCubedBlocks {
	public static final RotatedPillarBlock TEST_BLOCK = REGISTRAR.blocks.create("test_block", RotatedPillarBlock::new)
			.copyFrom(Blocks.STONE)
			.settings(QuiltBlockSettings::noCollision)
			.item(BlockItemProvider::noItem)
			.build();

	public static final FloorButtonBlock FLOOR_BUTTON_BLOCK = REGISTRAR.blocks.create("floor_button", FloorButtonBlock::new)
			.copyFrom(Blocks.STONE)
			.item((block, builder) -> REGISTRAR.items.create("floor_button", settings -> new MultiBlockItem((AbstractMultiBlock) block, settings)))
			.build();

	public static void init() {
	}
}
