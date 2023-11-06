package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

import org.quiltmc.loader.api.minecraft.ClientOnly;

public class LinkPortalsPacket implements ClientboundManagePortalsPacket {
	private final int aId;
	private final int bId;

	public LinkPortalsPacket(Portal a, Portal b) {
		this.aId = a.netId;
		this.bId = b.netId;
	}

	public LinkPortalsPacket(FriendlyByteBuf buf) {
		this.aId = buf.readVarInt();
		this.bId = buf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(aId);
		buf.writeVarInt(bId);
	}

	@Override
	public PacketType<?> getType() {
		return PortalCubedPackets.LINK_PORTALS;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, ClientPortalManager manager, PacketSender responder) {
		Portal a = manager.getPortalByNetId(aId);
		Portal b = manager.getPortalByNetId(bId);
		if (a != null && b != null) {
			manager.linkPortals(a, b);
		}
	}
}
