package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.player.LocalPlayer;

import org.quiltmc.loader.api.minecraft.ClientOnly;

public interface ClientboundManagePortalsPacket extends ClientboundPacket {
	@Override
	@ClientOnly
	default void handle(LocalPlayer player, PacketSender responder) {
		ClientPortalManager manager = ClientPortalManager.of(player.clientLevel);
		this.handle(player, manager, responder);
	}

	@ClientOnly
	void handle(LocalPlayer player, ClientPortalManager manager, PacketSender responder);
}
