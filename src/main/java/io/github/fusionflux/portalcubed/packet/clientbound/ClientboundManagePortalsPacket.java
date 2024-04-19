package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public interface ClientboundManagePortalsPacket extends ClientboundPacket {
	@ClientOnly
	@Override
	default void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ClientPortalManager manager = ClientPortalManager.of(player.clientLevel);
		this.handle(player, manager, responder);
	}

	@ClientOnly
	void handle(LocalPlayer player, ClientPortalManager manager, PacketSender<CustomPacketPayload> responder);
}
