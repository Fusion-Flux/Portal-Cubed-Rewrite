package io.github.fusionflux.portalcubed.packet.clientbound;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreatePortalPacket(PortalInstance portal) implements ClientboundManagePortalsPacket {
	public CreatePortalPacket(FriendlyByteBuf buf) {
		this(PortalInstance.fromNetwork(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		this.portal.toNetwork(buf);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.CREATE_PORTAL;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, ClientPortalManager manager, PacketSender<CustomPacketPayload> responder) {
		manager.addPortal(this.portal);
	}
}
