package io.github.fusionflux.portalcubed.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public interface ClientboundPacket extends BasePacket {
	@ClientOnly
	void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder);

	static void receive(Minecraft client, ClientPacketListener handler, ClientboundPacket payload,
			PacketSender<CustomPacketPayload> responder) {
		payload.handle(client.player, responder);
	}
}
