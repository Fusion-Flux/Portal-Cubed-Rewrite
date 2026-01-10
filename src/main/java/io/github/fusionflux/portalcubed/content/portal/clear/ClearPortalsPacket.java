package io.github.fusionflux.portalcubed.content.portal.clear;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public enum ClearPortalsPacket implements ServerboundPacket {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ClearPortalsPacket> CODEC = StreamCodec.unit(INSTANCE);

	public static final Component SUCCESS = Component.translatable("key.portalcubed.clear_portals.success");
	public static final Component FAIL = Component.translatable("key.portalcubed.clear_portals.fail");

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CLEAR_PORTALS;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		ServerPortalManager manager = player.serverLevel().portalManager();

		boolean removed = false;

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			PortalGunSettings settings = held.get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
			if (settings != null) {
				manager.setPair(settings.pairFor(player), null);
				removed = true;
			}
		}

		player.sendSystemMessage(removed ? SUCCESS : FAIL, true);
	}
}
