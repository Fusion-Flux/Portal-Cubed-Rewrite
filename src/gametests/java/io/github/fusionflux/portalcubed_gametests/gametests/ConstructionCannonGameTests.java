package io.github.fusionflux.portalcubed_gametests.gametests;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.panel.PanelMaterial;
import io.github.fusionflux.portalcubed.content.panel.PanelPart;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ConstructionCannonGameTests implements FabricGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":construction_cannon/";

	private static final CommonListenerCookie MOCK_PLAYER_COOKIE = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
	private static final ResourceLocation CONSTRUCT = PortalCubed.id("panels/white/white_2x2_panel");
	private static final ItemStack MATERIAL_ITEM = new ItemStack(PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE).get(PanelPart.SINGLE), 4);
	private static final BlockPos ASSERT_POS = new BlockPos(2, 1, 1);
	private static final Block ASSERT_BLOCK = PortalCubedBlocks.PANELS.get(PanelMaterial.WHITE).get(PanelPart.MULTI_2x2_BOTTOM_LEFT);

	private void commonTest(GameTestHelper helper, boolean creative, int materialAmount, boolean replaceMode) {
		ServerLevel world = helper.getLevel();
		MinecraftServer server = world.getServer();
		ServerPlayer player = new ServerPlayer(server, world, MOCK_PLAYER_COOKIE.gameProfile(), MOCK_PLAYER_COOKIE.clientInformation()) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return creative;
			}
		};
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		player.connection = new ServerGamePacketListenerImpl(server, connection, player, MOCK_PLAYER_COOKIE);

		TagKey<Item> material = MATERIAL_ITEM.getItemHolder()
				.tags()
				.findFirst()
				.orElseThrow(() -> new GameTestAssertException("No tags found for material item!"));
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Holder.direct(PortalCubedItems.CONSTRUCTION_CANNON), 1, DataComponentPatch.builder().set(
				PortalCubedDataComponents.CANNON_SETTINGS,
				CannonSettings.builder()
						.setMaterial(material)
						.setConstruct(CONSTRUCT)
						.setReplaceMode(replaceMode)
						.build()
		).build()));

		if (materialAmount > 0)
			player.addItem(MATERIAL_ITEM.copyWithCount(materialAmount));

		helper.useBlock(ASSERT_POS, player);
	}

	//Tests a creative player using a Construction Cannon in normal mode, with no materials
	@GameTest(template = GROUP + "construct_place_normal")
	public void constructPlaceCreative(GameTestHelper helper) {
		this.commonTest(helper, true, 0, false);
		helper.succeedWhenBlockPresent(ASSERT_BLOCK, ASSERT_POS);
	}

	//Tests a survival player using a Construction Cannon in normal mode, with the correct amount of blocks to place the structure
	@GameTest(template = GROUP + "construct_place_normal")
	public void constructPlaceNormal(GameTestHelper helper) {
		this.commonTest(helper, false, 4, false);
		helper.succeedWhenBlockPresent(ASSERT_BLOCK, ASSERT_POS);
	}

	//Tests a survival player using a Construction Cannon in replace mode, with the correct amount of blocks to place the structure
	@GameTest(template = GROUP + "construct_place_replace")
	public void constructPlaceReplace(GameTestHelper helper) {
		this.commonTest(helper, false, 4, true);
		helper.succeedWhenBlockPresent(ASSERT_BLOCK, ASSERT_POS);
	}

	//Tests a survival player using a Construction Cannon in normal mode, but the placement location is blocked
	@GameTest(template = GROUP + "construct_place_obstructed")
	public void constructPlaceObstructed(GameTestHelper helper) {
		this.commonTest(helper, false, 4, false);
		helper.succeedWhenBlockPresent(Blocks.AIR, ASSERT_POS);
	}

	//Tests a survival player using a Construction Cannon in normal mode, with no materials
	@GameTest(template = GROUP + "construct_place_no_material")
	public void constructPlaceNoMaterial(GameTestHelper helper) {
		this.commonTest(helper, false, 0, false);
		helper.succeedWhenBlockPresent(Blocks.AIR, ASSERT_POS);
	}

	//Tests a survival player using a Construction Cannon in normal mode, with not enough material
	@GameTest(template = GROUP + "construct_place_not_enough_material")
	public void constructPlaceNotEnoughMaterial(GameTestHelper helper) {
		this.commonTest(helper, false, 3, false);
		helper.succeedWhenBlockPresent(Blocks.AIR, ASSERT_POS);
	}

}
