package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;

import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.world.entity.Mob;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

public class BootsGameTests implements QuiltGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":boots/";

	private static final int TICKS_FOR_FALL = 50;

	//Test entities falling with/without the boots
	@GameTest(template = GROUP + "boots_test")
	public void bootsTest(GameTestHelper helper) {
		Mob entityWithBoots = spawnWithBoots(helper, EntityType.HUSK, new BlockPos(1, 48, 2));
		Mob entityWithNoBoots = helper.spawnWithNoFreeWill(EntityType.STRAY, new BlockPos(3, 48, 2));  //you're part of the control group, by the way

		//Delay by 50 ticks to give them a chance to hit the ground; it takes ~40 for this to happen
		helper.runAfterDelay(TICKS_FOR_FALL, () -> helper.succeedIf(() -> {
			helper.assertEntityPresent(entityWithBoots.getType());
			helper.assertEntityNotPresent(entityWithNoBoots.getType());
		}));
	}

	//Test entities falling with the boots onto dripstone
	@GameTest(template = GROUP + "boots_bypass_test")
	public void bootsBypassTest(GameTestHelper helper) {
		Mob dripstone = spawnWithBoots(helper, EntityType.HUSK, new BlockPos(1, 48, 2));
		Mob noDripstone = spawnWithBoots(helper, EntityType.STRAY, new BlockPos(3, 48, 2));  //you're part of the control group, by the way

		//Delay by 50 ticks to give them a chance to hit the ground; it takes ~40 for this to happen
		helper.runAfterDelay(TICKS_FOR_FALL, () -> helper.succeedIf(() -> {
			helper.assertEntityPresent(noDripstone.getType());
			helper.assertEntityNotPresent(dripstone.getType());
		}));
	}

	public static <E extends Mob> E spawnWithBoots(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
		return Util.make(helper.spawnWithNoFreeWill(type, pos), e -> e.setItemSlot(EquipmentSlot.FEET, PortalCubedItems.LONG_FALL_BOOTS.getDefaultInstance()));
	}
}
