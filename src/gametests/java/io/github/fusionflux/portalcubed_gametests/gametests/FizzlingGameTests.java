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

	//Test hoppers and entities being unable to pick up fizzling items
	//Note - potentially add a similar test for picking up shot arrows and thrown tridents that are being fizzled, if there's a way to do that in a gametest
	@GameTest(template = GROUP + "fizzling_item_pickup", timeoutTicks = 200)
	public void fizzlingItemPickup(GameTestHelper helper) {
		helper.pullLever(3, 5, 3);

		helper.runAfterDelay(40, () -> {  //Wait for items to fall before fizzling them
			helper.pullLever(1, 2, 0);

			helper.runAfterDelay(70, helper::killAllEntities);  //Give the hopper minecart and fox time to attempt to pick up the items before killing them

			helper.runAfterDelay(90, () -> helper.succeedWhen(() -> {  //Only do final checks after the fox and hopper minecart have been killed; this runs as soon as the items start fizzling otherwise
				helper.assertEntitiesPresent(EntityType.ITEM, 0);  //Make sure no items exist after the minecart/fox are killed, indicating they never picked anything up
				helper.assertBlockProperty(new BlockPos(5, 2, 4), RedstoneLampBlock.LIT, false);  //Make sure the hopper block is empty, indicating it never picked anything up
			}));
		});
	}

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
