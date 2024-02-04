package io.github.fusionflux.portalcubed.packet;

import org.quiltmc.qsl.networking.api.PacketSender;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public interface ServerboundPacket extends BasePacket {
	void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder);

	static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
			ServerboundPacket payload, PacketSender<CustomPacketPayload> responder) {
		payload.handle(player, responder);
	}
}
