package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.Item;

import net.minecraft.world.level.block.Blocks;

import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ConstructionCannonGameTests implements QuiltGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":construction_cannon/";

	@SuppressWarnings("deprecation") // builtInRegistryHolder
	public static ItemStack createCannon(ResourceLocation construct, PanelMaterial material, Boolean replace_mode) {
		Item materialItem = PortalCubedBlocks.PANELS.get(material).get(PanelPart.SINGLE).asItem();
		ItemStack cannon = PortalCubedItems.CONSTRUCTION_CANNON.getDefaultInstance();
		materialItem.builtInRegistryHolder().tags()
				.filter(ConstructManager.INSTANCE.getMaterials()::contains)
				.findFirst()
				.ifPresent(tag -> ConstructionCannonItem.setCannonSettings(cannon, CannonSettings.DEFAULT
						.withMaterial(tag)
						.withConstruct(construct)
						.withReplaceMode(replace_mode)
				));
		return cannon;
	}

	//If a test is checking that a construct is not placed, don't pass instantly because the position also starts as air
	private static final int TICKS_FOR_CONSTRUCT_PLACE = 10;

	//Tests a survival mockplayer using a Construction Cannon in normal mode, with the correct amount of blocks to place the structure
	@GameTest(template = GROUP + "construct_place_normal")
	public void constructPlaceNormal(GameTestHelper helper) {

		Player gerald = helper.makeMockSurvivalPlayer();
		gerald.setItemInHand(InteractionHand.MAIN_HAND, createCannon(PortalCubed.id("panels/white/white_2x2_panel"), PanelMaterial.WHITE, false));
		gerald.getInventory().add(new ItemStack(PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE).get(PanelPart.SINGLE).asItem(), 4));
		helper.useBlock(new BlockPos(2, 2, 1), gerald);

		helper.succeedIf(() -> {
			//add check for remaining items here max, should be 0
			helper.assertBlockNotPresent(Blocks.AIR, 2, 2, 1);
		});
	}

	//Tests a survival mockplayer using a Construction Cannon in replace mode, with the correct amount of blocks to place the structure
	@GameTest(template = GROUP + "construct_place_replace")
	public void constructPlaceReplace(GameTestHelper helper) {

		Player gerald = helper.makeMockSurvivalPlayer();
		gerald.setItemInHand(InteractionHand.MAIN_HAND, createCannon(PortalCubed.id("panels/white/white_2x2_panel"), PanelMaterial.WHITE, true));
		gerald.getInventory().add(new ItemStack(PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE).get(PanelPart.SINGLE).asItem(), 4));
		helper.useBlock(new BlockPos(2, 2, 1), gerald);

		helper.succeedIf(() -> {
			//add check for remaining items here max, should be 0
			helper.assertBlockNotPresent(PortalCubedBlocks.OFFICE_CONCRETE, 2, 2, 1);
		});
	}

	//Tests a survival mockplayer using a Construction Cannon in normal mode, but the placement location is blocked
	@GameTest(template = GROUP + "construct_place_obstructed")
	public void constructPlaceObstructed(GameTestHelper helper) {

		Player gerald = helper.makeMockSurvivalPlayer();
		gerald.setItemInHand(InteractionHand.MAIN_HAND, createCannon(PortalCubed.id("panels/white/white_2x2_panel"), PanelMaterial.WHITE, false));
		gerald.getInventory().add(new ItemStack(PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE).get(PanelPart.SINGLE).asItem(), 4));
		helper.useBlock(new BlockPos(2, 2, 1), gerald);

		helper.runAfterDelay(TICKS_FOR_CONSTRUCT_PLACE, () -> {
			//add check for remaining items here max, should be 4
			helper.succeedWhenBlockPresent(Blocks.AIR, 2, 2, 1);
		});
	}

	//Tests a survival mockplayer using a Construction Cannon in normal mode, with 0 of the required blocks to place the structure
	@GameTest(template = GROUP + "construct_place_no_material")
	public void constructPlaceNoMaterial(GameTestHelper helper) {

		Player gerald = helper.makeMockSurvivalPlayer();
		gerald.setItemInHand(InteractionHand.MAIN_HAND, createCannon(PortalCubed.id("panels/white/white_2x2_panel"), PanelMaterial.WHITE, false));
		helper.useBlock(new BlockPos(2, 2, 1), gerald);

		helper.runAfterDelay(TICKS_FOR_CONSTRUCT_PLACE, () -> {
			helper.succeedWhenBlockPresent(Blocks.AIR, 2, 2, 1);
		});
	}

	//Tests a survival mockplayer using a Construction Cannon in normal mode, with 1 of the required blocks to place the structure (4 needed)
	@GameTest(template = GROUP + "construct_place_not_enough_material")
	public void constructPlaceNotEnoughMaterial(GameTestHelper helper) {

		Player gerald = helper.makeMockSurvivalPlayer();
		gerald.setItemInHand(InteractionHand.MAIN_HAND, createCannon(PortalCubed.id("panels/white/white_2x2_panel"), PanelMaterial.WHITE, false));
		gerald.getInventory().add(new ItemStack(PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE).get(PanelPart.SINGLE).asItem(), 1));
		helper.useBlock(new BlockPos(2, 2, 1), gerald);

		helper.runAfterDelay(TICKS_FOR_CONSTRUCT_PLACE, () -> {
			//add check for remaining items here max, should be 1
			helper.succeedWhenBlockPresent(Blocks.AIR, 2, 2, 1);
		});
	}

}
