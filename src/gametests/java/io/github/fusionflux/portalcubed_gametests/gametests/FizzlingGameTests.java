package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.RedstoneLampBlock;

public class FizzlingGameTests implements QuiltGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":fizzling/";

	//Test container items dropping their contents when fizzled
	@GameTest(template = GROUP + "fizzle_container")
	public void fizzleContainer(GameTestHelper helper) {
		helper.pullLever(2, 5, 4);

		helper.runAfterDelay(80, () -> {  //Wait for items to be fizzled before unlocking the collection hopper
			helper.pullLever(2, 2, 3);

			helper.succeedWhen(() -> {
				helper.assertBlockProperty(new BlockPos(1, 2, 1), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(3, 2, 1), RedstoneLampBlock.LIT, true);
			});
		});
	}

	//Test hoppers and certain entity types being unable to pick up fizzling items
	  // waiting on a fix for this

	//Test fizzling items emitting a vibration properly
	@GameTest(template = GROUP + "fizzle_vibration")
	public void fizzleVibration(GameTestHelper helper) {
		Entity armorStand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(3, 2, 3));

		helper.runAfterDelay(20, () -> {
			FizzleBehaviour.DISINTEGRATION.fizzle(armorStand);
			helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(1, 1, 1), RedstoneLampBlock.LIT, true));
		});
	}
}
