package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.RedstoneLampBlock;

public class ButtonGameTests implements FabricGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":buttons/";

	//Tests wind charges being able to activate pedestal buttons
	@GameTest(template = GROUP + "wind_charge_button")
	public void windChargeButton(GameTestHelper helper) {
		helper.pullLever(0, 1, 2);
		helper.pullLever(4, 5, 2);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(4, 1, 1), RedstoneLampBlock.LIT, false);
			helper.assertBlockProperty(new BlockPos(0, 5, 3), RedstoneLampBlock.LIT, false);
		});
	}
}
