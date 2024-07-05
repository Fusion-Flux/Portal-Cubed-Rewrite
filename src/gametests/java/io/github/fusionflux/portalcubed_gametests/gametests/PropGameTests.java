package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.RedstoneLampBlock;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

public class PropGameTests implements QuiltGameTest {
	private static final String group = PortalCubedGameTests.ID + ":props/";

	@GameTest(template = group + "floor_button_cube")
	public void floorButtonCube(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 3, 0), Blocks.AIR);
		helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true));
	}
	@GameTest(template = group + "floor_button_entity")
	public void floorButtonEntity(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 3, 0), Blocks.AIR);
		helper.succeedWhen(() -> helper.assertBlockProperty(new BlockPos(0, 2, 0), RedstoneLampBlock.LIT, true));
	}
	@GameTest(template = group + "fizzle_goo")
	public void fizzleGoo(GameTestHelper helper) {
		Prop storageCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.STORAGE_CUBE), (new BlockPos(1, 4, 1)));

		helper.succeedWhen(() -> {
			helper.assertEntityNotPresent(storageCube.getType());
		});

	}
	@GameTest(template = group + "burn_companion_cube")
	public void burnCompanionCube(GameTestHelper helper) {
		Prop lavaCompanionCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.PORTAL_1_COMPANION_CUBE), (new BlockPos(1, 4, 1)));
		Prop fireCompanionCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.PORTAL_1_COMPANION_CUBE), (new BlockPos(3, 4, 1)));
		Prop cauldronCompanionCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.PORTAL_1_COMPANION_CUBE), (new BlockPos(5, 4, 1)));

		helper.succeedWhen(() -> {
			helper.assertEntityProperty(lavaCompanionCube, Prop::getVariant, "Variant", 1);
			helper.assertEntityProperty(fireCompanionCube, Prop::getVariant, "Variant", 1);
			helper.assertEntityProperty(cauldronCompanionCube, Prop::getVariant, "Variant", 1);
		});

	}
	@GameTest(template = group + "prop_washing")
	public void prop_washing(GameTestHelper helper) {
		Prop p2StorageCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.STORAGE_CUBE), (new BlockPos(1, 3, 1)));
		Prop p2CompanionCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.COMPANION_CUBE), (new BlockPos(1, 3, 2)));
		Prop radio = helper.spawn(PortalCubedEntities.PROPS.get(PropType.RADIO), (new BlockPos(1, 3, 3)));
		Prop p1CompanionCube = helper.spawn(PortalCubedEntities.PROPS.get(PropType.PORTAL_1_COMPANION_CUBE), (new BlockPos(1, 3, 4)));

		p2StorageCube.setVariant(2);
		p2CompanionCube.setVariant(2);
		p2CompanionCube.setSilent(true);
		radio.setVariant(1);
		radio.setSilent(true);
		p1CompanionCube.setVariant(1);
		helper.succeedWhen(() -> {
			helper.assertEntityProperty(p2StorageCube, Prop::getVariant, "Variant", 0);
			helper.assertEntityProperty(p2CompanionCube, Prop::getVariant, "Variant", 0);
			helper.assertEntityProperty(radio, Prop::getVariant, "Variant", 0);
			helper.assertEntityProperty(p1CompanionCube, Prop::getVariant, "Variant", 0);
		});
	}
}
