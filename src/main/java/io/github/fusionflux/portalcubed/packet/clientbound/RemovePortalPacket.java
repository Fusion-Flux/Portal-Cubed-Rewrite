package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

public class RemovePortalPacket implements ClientboundManagePortalsPacket {
	private final int portalId;

	public RemovePortalPacket(Portal portal) {
		this.portalId = portal.netId;
	}

	public RemovePortalPacket(FriendlyByteBuf buf) {
		this.portalId = buf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(portalId);
	}

	@Override
	public PacketType<?> getType() {
		return PortalCubedPackets.REMOVE_PORTAL;
	}

	@Override
	public void handle(LocalPlayer player, ClientPortalManager manager, PacketSender responder) {
		manager.removePortal(portalId);
	}
}
