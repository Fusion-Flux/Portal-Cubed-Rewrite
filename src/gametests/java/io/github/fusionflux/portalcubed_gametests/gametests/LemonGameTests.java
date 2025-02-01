package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public class LemonGameTests implements FabricGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":lemons/";

	//Tests lemon saplings growing
	@GameTest(template = GROUP + "lemon_sapling_grow")
	public void lemonSaplingGrow(GameTestHelper helper) {
		//Should probably clean this up later; currently sets a redstone block to push an observer clock into place to dispense bonemeal
		helper.setBlock(6, 1, 1, Blocks.REDSTONE_BLOCK);
		helper.succeedWhenBlockPresent(PortalCubedBlocks.LEMON_LOG, 4, 1, 4);
	}

	//Tests lemonades being fired out of dispensers
	@GameTest(template = GROUP + "lemonade_dispenser")
	public void lemonadeDispenser(GameTestHelper helper) {
		helper.pullLever(1, 1, 0);
		helper.succeedWhen(() ->
				helper.assertEntityPresent(PortalCubedEntities.LEMONADE));
	}
}
