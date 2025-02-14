package io.github.fusionflux.portalcubed.packet.clientbound;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdatePortalPairPacket(String key, @Nullable PortalPair pair) implements ClientboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePortalPairPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, UpdatePortalPairPacket::key,
			PortalCubedStreamCodecs.nullable(PortalPair.STREAM_CODEC), UpdatePortalPairPacket::pair,
			UpdatePortalPairPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.UPDATE_PORTAL_PAIR;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context ctx) {
		ClientPortalManager manager = ctx.player().clientLevel.portalManager();
		manager.setSyncedPair(this.key, this.pair);
	}
}
