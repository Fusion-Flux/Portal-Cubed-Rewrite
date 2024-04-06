package io.github.fusionflux.portalcubed.packet.clientbound;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreatePortalPacket(Portal portal) implements ClientboundPacket {
	public CreatePortalPacket(FriendlyByteBuf buf) {
		this(Portal.fromNetwork(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		this.portal.toNetwork(buf);
	}

	@Override
	public ResourceLocation id() {
		return PortalCubedPackets.CREATE_PORTAL;
	}

	@ClientOnly
	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ClientPortalManager manager = ClientPortalManager.of(player.clientLevel);
		manager.addPortal(this.portal);
	}
}
