package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.RedstoneLampBlock;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

public class PropGameTests implements QuiltGameTest {
	private static final String group = PortalCubedGameTests.ID + ":props/";

	@GameTest(template = group + "floor_button_cube")
	public void floorButtonCube(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 3, 0), Blocks.AIR);
		helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true));
	}
}
