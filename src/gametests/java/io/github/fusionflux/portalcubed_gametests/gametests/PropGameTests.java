package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.RedstoneLampBlock;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

public class PropGameTests implements QuiltGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":props/";

	@GameTest(template = GROUP + "floor_button_cube")
	public void floorButtonCube(GameTestHelper helper) {
		this.floorButton(helper);
	}

	@GameTest(template = GROUP + "floor_button_entity")
	public void floorButtonEntity(GameTestHelper helper) {
		this.floorButton(helper);
	}

	private void floorButton(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 3, 0), Blocks.AIR);
		helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true));
	}

	@GameTest(template = GROUP + "fizzle_goo")
	public void fizzleGoo(GameTestHelper helper) {
		Prop storageCube = spawnProp(helper, PropType.STORAGE_CUBE, new BlockPos(1, 4, 1));
		helper.succeedWhen(() -> helper.assertEntityNotPresent(storageCube.getType()));
	}

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
