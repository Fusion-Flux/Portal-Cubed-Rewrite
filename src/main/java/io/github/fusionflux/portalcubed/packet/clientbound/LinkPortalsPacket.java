package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public class LinkPortalsPacket implements ClientboundManagePortalsPacket {
	private final int aId;
	private final int bId;

	public LinkPortalsPacket(PortalInstance a, PortalInstance b) {
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
	public ResourceLocation getId() {
		return PortalCubedPackets.LINK_PORTALS;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, ClientPortalManager manager, PacketSender<CustomPacketPayload> responder) {
		PortalInstance a = manager.getPortalByNetId(aId);
		PortalInstance b = manager.getPortalByNetId(bId);
		if (a != null && b != null) {
			manager.linkPortals(a, b);
		}
	}
}
