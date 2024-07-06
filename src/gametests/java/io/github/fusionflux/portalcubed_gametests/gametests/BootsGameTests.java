package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.EquipmentSlot;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

public class BootsGameTests implements QuiltGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":boots/";

	//Test entities falling with/without the boots
	@GameTest(template = GROUP + "boots_test")
	public void bootsTest(GameTestHelper helper) {
		Entity entityWithBoots = spawnWithBoots(helper, EntityType.HUSK, new BlockPos(1, 48, 2));
		Entity entityWithNoBoots = helper.spawn(EntityType.STRAY, new BlockPos(3, 48, 2));  //you're part of the control group, by the way

		//Delay by 50 ticks to give them a chance to hit the ground; it takes ~40 for this to happen
		helper.runAfterDelay(50, () -> {
			helper.succeedWhen(() -> {
						helper.assertEntityPresent(entityWithBoots.getType());
						helper.assertEntityNotPresent(entityWithNoBoots.getType());
					}
			);
		});

	}

	//Test entities falling with the boots onto dripstone
	@GameTest(template = GROUP + "boots_bypass_test")
	public void bootsBypassTest(GameTestHelper helper) {
		Entity dripstone = spawnWithBoots(helper, EntityType.HUSK, new BlockPos(1, 48, 2));
		Entity noDripstone = spawnWithBoots(helper, EntityType.STRAY, new BlockPos(3, 48, 2));  //you're part of the control group, by the way

		//Delay by 50 ticks to give them a chance to hit the ground; it takes ~40 for this to happen
		helper.runAfterDelay(50, () -> {
			helper.succeedWhen(() -> {
					helper.assertEntityPresent(noDripstone.getType());
					helper.assertEntityNotPresent(dripstone.getType());
				}
			);
		});
	}

	public static <T extends Entity> T spawnWithBoots(GameTestHelper helper, EntityType<T> type, BlockPos pos) {
		return Util.make(helper.spawn(type, pos), e -> e.setItemSlot(EquipmentSlot.FEET, PortalCubedItems.LONG_FALL_BOOTS.getDefaultInstance()));
	}
}
