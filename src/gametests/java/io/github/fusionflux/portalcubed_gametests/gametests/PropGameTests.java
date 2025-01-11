package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed_gametests.Batches;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.Vec3;

public class PropGameTests implements FabricGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":props/";

	//Delay entity-on-button tests by 20 ticks to give the entities time to fall onto the buttons before checking states
	private static final int TICKS_FOR_BUTTON_LAND = 20;

	//Test for prop interaction on buttons.  Cubes should press, non-cubes should not.
	@GameTest(template = GROUP + "floor_button_cube")
	public void floorButtonCube(GameTestHelper helper) {
		spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(2, 3, 0));
		spawnProp(helper, PropType.BEANS, new BlockPos(2, 3, 4));
		helper.runAfterDelay(TICKS_FOR_BUTTON_LAND, () ->
			helper.succeedWhen(() -> {
				helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(0, 2, 4), RedstoneLampBlock.LIT, false);
		}));
	}

	//Test for prop interaction on cube buttons.  Cubes should press, non-cubes should not.
	@GameTest(template = GROUP + "cube_button")
	public void cubeButton(GameTestHelper helper) {
		spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(2, 3, 0));
		spawnProp(helper, PropType.BEANS, new BlockPos(2, 3, 4));
		helper.runAfterDelay(TICKS_FOR_BUTTON_LAND, () ->
				helper.succeedWhen(() -> {
					helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true);
					helper.assertBlockProperty(new BlockPos(0, 2, 4), RedstoneLampBlock.LIT, false);
				}));
	}

	//Test for cubes falling out of wall cube buttons.
	@GameTest(template = GROUP + "wall_cube_button")
	public void wallCubeButton(GameTestHelper helper) {
		Prop gerald = spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(3, 2, 0));
		Vec3 wallButtonPos = helper.absoluteVec(new Vec3(2, 3.2, 1.5));
		gerald.setPos(wallButtonPos);
		helper.runAfterDelay(TICKS_FOR_BUTTON_LAND, () ->
				helper.succeedWhen(() -> {
					helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true);
					helper.assertBlockProperty(new BlockPos(3, 3, 2), RedstoneLampBlock.LIT, false);
				}));
	}

	//Test for fizzled cubes being pushed away from buttons.
	@GameTest(template = GROUP + "fizzle_prop_on_button")
	public void fizzlePropOnButton(GameTestHelper helper) {
		Prop gerald = spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(1, 3, 0));
		Prop aSecondGeraldHasHitTheGametest = spawnProp(helper, PropType.COMPANION_CUBE, new BlockPos(1, 3, 4));

		helper.runAfterDelay(TICKS_FOR_BUTTON_LAND, () -> {
			FizzleBehaviour.DISINTEGRATION.fizzle(gerald);
			FizzleBehaviour.DISINTEGRATION.fizzle(aSecondGeraldHasHitTheGametest);

			helper.succeedWhen(() -> {
				helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, false);
				helper.assertBlockProperty(new BlockPos(0, 2, 4), RedstoneLampBlock.LIT, false);
			});
		});
	}

	//Test for entity interaction on buttons.  Anything that presses a stone pressure plate should press buttons.
	@GameTest(template = GROUP + "floor_button_entity")
	public void floorButtonEntity(GameTestHelper helper) {
		helper.spawn(EntityType.ARMOR_STAND, new BlockPos(2, 3, 0));
		helper.spawn(EntityType.ARROW, new BlockPos(2, 3, 4));
		helper.runAfterDelay(TICKS_FOR_BUTTON_LAND, () ->
			helper.succeedWhen(() -> {
				helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(0, 2, 4), RedstoneLampBlock.LIT, false);
		}));
	}

	//Test for props being fizzled by goo.  Also checks a prop in the goo immunity tag
	@GameTest(template = GROUP + "fizzle_goo")
	public void fizzleGoo(GameTestHelper helper) {
		Prop storageCube = spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(1, 4, 1));
		Prop radio = spawnProp(helper, PropType.RADIO, new BlockPos(1, 4, 3));
		helper.succeedWhen(() -> {
			helper.assertEntityNotPresent(storageCube.getType());
			helper.assertEntityPresent(radio.getType());
		});
	}

	//Tests for companion cubes becoming charred when in contact with fire or lava
	@GameTest(template = GROUP + "burn_companion_cube")
	public void burnCompanionCube(GameTestHelper helper) {
		Prop lavaCompanionCube = spawnProp(helper, PropType.PORTAL_1_COMPANION_CUBE, new BlockPos(1, 4, 1));
		Prop fireCompanionCube = spawnProp(helper, PropType.PORTAL_1_COMPANION_CUBE, new BlockPos(3, 4, 1));
		Prop cauldronCompanionCube = spawnProp(helper, PropType.PORTAL_1_COMPANION_CUBE, new BlockPos(5, 4, 1));
		helper.succeedWhen(() -> {
			assertPropVariant(helper, lavaCompanionCube, 1);
			assertPropVariant(helper, fireCompanionCube, 1);
			assertPropVariant(helper, cauldronCompanionCube, 1);
		});
	}

	//Tests for dirty/charred props being washed when dropped into water
	//Note - add redirection cubes to this once they get added.
	@GameTest(template = GROUP + "prop_washing")
	public void propWashing(GameTestHelper helper) {
		Prop p2StorageCube = spawnDirtyProp(helper, PropType.STORAGE_CUBE, new BlockPos(1, 3, 1));
		Prop p2CompanionCube = spawnDirtyProp(helper, PropType.COMPANION_CUBE, new BlockPos(1, 3, 2));
		Prop radio = spawnDirtyProp(helper, PropType.RADIO, new BlockPos(1, 3, 3));
		Prop p1CompanionCube = spawnDirtyProp(helper, PropType.PORTAL_1_COMPANION_CUBE, new BlockPos(1, 3, 4));
		helper.succeedWhen(() -> {
			assertPropVariant(helper, p2StorageCube, 0);
			assertPropVariant(helper, p2CompanionCube, 0);
			assertPropVariant(helper, radio, 0);
			assertPropVariant(helper, p1CompanionCube, 0);
		});
	}

	//Tests for dirty/charred props being washed when the weather is rain
	//Note - add redirection cubes to this once they get added
	@GameTest(template = GROUP + "prop_washing_in_rain", batch = Batches.RAINY)
	public void propWashingInRain(GameTestHelper helper) {
		Prop p2StorageCube = spawnDirtyProp(helper, PropType.STORAGE_CUBE, new BlockPos(1, 3, 1));
		Prop p2CompanionCube = spawnDirtyProp(helper, PropType.COMPANION_CUBE, new BlockPos(1, 3, 2));
		Prop radio = spawnDirtyProp(helper, PropType.RADIO, new BlockPos(1, 3, 3));
		Prop p1CompanionCube = spawnDirtyProp(helper, PropType.PORTAL_1_COMPANION_CUBE, new BlockPos(1, 3, 4));

		helper.succeedWhen(() -> {
			assertPropVariant(helper, p2StorageCube, 0);
			assertPropVariant(helper, p2CompanionCube, 0);
			assertPropVariant(helper, radio, 0);
			assertPropVariant(helper, p1CompanionCube, 0);
		});
	}

	//Tests the interaction of dirtying a prop with moss/vines
	//Note - add redirection cubes to this once they get added
	@GameTest(template = GROUP + "prop_dirtying")
	public void propDirtying(GameTestHelper helper) {
		Prop p2StorageCube = spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(1, 3, 1));
		Prop p2CompanionCube = spawnProp(helper, PropType.COMPANION_CUBE, new BlockPos(1, 3, 2));
		Prop radio = spawnProp(helper, PropType.RADIO, new BlockPos(1, 3, 3));

		Player gerald = helper.makeMockPlayer(GameType.SURVIVAL);
		gerald.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MOSS_BLOCK, 3));
		p2StorageCube.interact(gerald, InteractionHand.MAIN_HAND);
		p2CompanionCube.interact(gerald, InteractionHand.MAIN_HAND);
		radio.interact(gerald, InteractionHand.MAIN_HAND);

		helper.succeedIf(() -> {
			ItemStack material = gerald.getMainHandItem();
			if (!material.isEmpty())
				throw new GameTestAssertException("Material used to dirty prop was not consumed : " + material);
			assertPropVariant(helper, p2StorageCube, 2);
			assertPropVariant(helper, p2CompanionCube, 2);
			assertPropVariant(helper, radio, 1);
		});
	}

	//Test for removing props with/without a hammer
	@GameTest(template = GROUP + "prop_removal")
	public void propRemoval(GameTestHelper helper) {
		Prop hammeredCube = spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(2, 2, 1));
		Prop smackedCube = spawnProp(helper, PropType.PORTAL_1_STORAGE_CUBE, new BlockPos(1, 2, 1));

		Player gerald = helper.makeMockPlayer(GameType.SURVIVAL);
		Player geraldTwoTheLongAwaitedSequel = helper.makeMockPlayer(GameType.SURVIVAL);
		gerald.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PortalCubedItems.HAMMER));
		gerald.attack(hammeredCube);
		geraldTwoTheLongAwaitedSequel.attack(smackedCube);

		helper.succeedIf(() -> {
			helper.assertEntityPresent(smackedCube.getType());
			helper.assertEntityNotPresent(hammeredCube.getType());
		});
	}

	//Test props being spawned from dispensers
	@GameTest(template = GROUP + "prop_dispenser")
	public void propDispenser(GameTestHelper helper) {
		helper.pullLever(1, 2, 0);
		helper.succeedWhenEntityPresent(PortalCubedEntities.PROPS.get(PropType.STORAGE_CUBE), 1, 3, 1);
	}

	//Test portal 1 props dealing damage when landing on entities
	@GameTest(template = GROUP + "falling_prop")
	public void fallingProp(GameTestHelper helper) {
		Mob gerald = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 2, 1));
		spawnProp(helper, PropType.PORTAL_1_STORAGE_CUBE, new BlockPos(1, 16, 1));

		helper.runAfterDelay(40, () -> helper.succeedIf(() -> { //Wait for cube to bonk Gerald
			helper.assertEntityNotPresent(gerald.getType());
		}));
	}

	public static Prop spawnProp(GameTestHelper helper, PropType type, BlockPos pos) {
		return Util.make(helper.spawn(type.entityType(), pos), p -> p.setSilent(true));
	}

	public static Prop spawnDirtyProp(GameTestHelper helper, PropType type, BlockPos pos) {
		return Util.make(spawnProp(helper, type, pos), p -> p.setDirty(true));
	}

	public static void assertPropVariant(GameTestHelper helper, Prop prop, int expectedVariant) {
		helper.assertEntityProperty(prop, Prop::getVariant, "variant", expectedVariant);
	}
}
