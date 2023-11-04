package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

public class CreatePortalPacket implements ClientboundPacket {
	private final Portal portal;

	public CreatePortalPacket(Portal portal) {
		this.portal = portal;
	}

	public CreatePortalPacket(FriendlyByteBuf buf) {
		this.portal = Portal.fromNetwork(buf);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		this.portal.toNetwork(buf);
	}

	@Override
	public PacketType<?> getType() {
		return PortalCubedPackets.CREATE_PORTAL;
	}

	@Override
	public void handle(LocalPlayer player, PacketSender responder) {
		ClientPortalManager manager = ClientPortalManager.of(player.clientLevel);
		manager.addPortal(this.portal);
	}
}
