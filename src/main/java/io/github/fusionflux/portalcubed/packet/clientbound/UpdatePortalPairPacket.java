package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.UUID;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdatePortalPairPacket(UUID uuid, @Nullable PortalPair pair) implements ClientboundPacket {
	public UpdatePortalPairPacket(FriendlyByteBuf buf) {
		this(buf.readUUID(), buf.readNullable(buffer -> buf.readJsonWithCodec(PortalPair.CODEC)));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(this.uuid);
		buf.writeNullable(this.pair, (buffer, pair) -> buffer.writeJsonWithCodec(PortalPair.CODEC, pair));
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.UPDATE_PORTAL_PAIR;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ClientPortalManager manager = player.clientLevel.portalManager();
		manager.setSyncedPair(this.uuid, this.pair);
	}
}
