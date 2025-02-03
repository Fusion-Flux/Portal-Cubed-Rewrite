package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.RedstoneLampBlock;

public class FizzlingGameTests implements FabricGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":fizzling/";

	//Tests container items dropping their contents when fizzled
	@GameTest(template = GROUP + "fizzle_container")
	public void fizzleContainer(GameTestHelper helper) {
		helper.pullLever(2, 4, 4);

		helper.runAfterDelay(80, () -> {  //Wait for items to be fizzled before unlocking the collection hopper
			helper.pullLever(2, 1, 3);

			helper.succeedWhen(() -> {
				helper.assertBlockProperty(new BlockPos(1, 1, 1), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(3, 1, 1), RedstoneLampBlock.LIT, true);
			});
		});
	}

	//Tests armor being dropped when equipped entities are fizzled
	//Note - Currently, this only works for armor stands.  Once this changes (1.21.5?), expand this test to cover other cases.
	@GameTest(template = GROUP + "fizzle_armor_drop")
	public void fizzleArmorDrop(GameTestHelper helper) {
		helper.pullLever(2, 4, 4);

		helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(2, 1, 1), RedstoneLampBlock.LIT, true));
	}

	//Tests leads dropping when a leashed entity is fizzled
	@GameTest(template = GROUP + "fizzle_lead_drop")
	public void fizzleLeadDrop(GameTestHelper helper) {

		helper.pullLever(1, 5, 4);
		helper.runAfterDelay(20, () -> helper.pullLever(3, 5, 4));

		helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(2, 1, 0), RedstoneLampBlock.LIT, true));
	}

	//Tests on-death potion effects activating when an entity is fizzled
	@GameTest(template = GROUP + "fizzle_death_effects")
	public void fizzleDeathEffects(GameTestHelper helper) {

		helper.pullLever(2, 6, 3);
		helper.runAfterDelay(20, () -> helper.pullLever(3, 6, 0));

		helper.runAfterDelay(20, () -> { //Don't run as soon as the test starts to avoid false positives from the observers as the test is placed
			helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(3, 0, 3), RedstoneLampBlock.LIT, true));
		});
	}

	//Tests hoppers and entities being unable to pick up fizzling items
	//Note - potentially add a similar test for picking up shot arrows and thrown tridents that are being fizzled, if there's a way to do that in a gametest
	@GameTest(template = GROUP + "fizzling_item_pickup", timeoutTicks = 200)
	public void fizzlingItemPickup(GameTestHelper helper) {
		helper.pullLever(3, 4, 3);

		helper.runAfterDelay(40, () -> {  //Wait for items to fall before fizzling them
			helper.pullLever(1, 1, 0);

			helper.runAfterDelay(70, helper::killAllEntities);  //Give the hopper minecart and fox time to attempt to pick up the items before killing them

			helper.runAfterDelay(90, () -> helper.succeedWhen(() -> {  //Only do final checks after the fox and hopper minecart have been killed; this runs as soon as the items start fizzling otherwise
				helper.assertEntitiesPresent(EntityType.ITEM, 0);  //Make sure no items exist after the minecart/fox are killed, indicating they never picked anything up
				helper.assertBlockProperty(new BlockPos(5, 1, 4), RedstoneLampBlock.LIT, false);  //Make sure the hopper block is empty, indicating it never picked anything up
			}));
		});
	}

	//Tests entities emitting a vibration when fizzled
	@GameTest(template = GROUP + "fizzle_vibration")
	public void fizzleVibration(GameTestHelper helper) {
		Entity armorStand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(3, 1, 3));

		helper.runAfterDelay(20, () -> {
			FizzleBehaviour.DISINTEGRATION.fizzle(armorStand);
			helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(1, 0, 1), RedstoneLampBlock.LIT, true));
		});
	}
}
