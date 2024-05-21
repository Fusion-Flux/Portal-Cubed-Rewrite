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

public class RemovePortalPacket implements ClientboundManagePortalsPacket {
	private final int portalId;

	public RemovePortalPacket(PortalInstance portal) {
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
	public ResourceLocation getId() {
		return PortalCubedPackets.REMOVE_PORTAL;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, ClientPortalManager manager, PacketSender<CustomPacketPayload> responder) {
		manager.removePortal(portalId);
	}
}
