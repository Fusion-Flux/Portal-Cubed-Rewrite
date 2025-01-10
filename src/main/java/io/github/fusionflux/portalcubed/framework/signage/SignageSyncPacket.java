package io.github.fusionflux.portalcubed.framework.signage;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SignageSyncPacket(Map<ResourceLocation, Signage> entries) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, SignageSyncPacket> CODEC = PortalCubedStreamCodecs.map(
			ResourceLocation.STREAM_CODEC, Signage.STREAM_CODEC
	).map(SignageSyncPacket::new, SignageSyncPacket::entries);

	@Override
	@NotNull
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.SYNC_SIGNAGE;
	}

	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		SignageManager.INSTANCE.readFromPacket(this);
	}
}
