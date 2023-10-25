package io.github.fusionflux.portalcubed_test.gametest;

import io.github.fusionflux.portalcubed_test.PortalCubedTestmod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

public class ExampleModGameTest implements QuiltGameTest {
	@GameTest(template = PortalCubedTestmod.ID + ":minecart_test")
	public void minecartTest(GameTestHelper helper) {
		helper.setBlock(2, 2, 0, Blocks.REDSTONE_BLOCK);
		helper.succeedWhenEntityPresent(EntityType.MINECART, 2, 2, 3);
	}
}
