package io.github.fusionflux.portalcubed.framework.construct;

import java.util.Map;

import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConstructSyncPacket(Map<Identifier, ConstructSet> constructs) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, ConstructSyncPacket> CODEC = PortalCubedStreamCodecs.map(
			Identifier.STREAM_CODEC, ConstructSet.STREAM_CODEC
	).map(ConstructSyncPacket::new, ConstructSyncPacket::constructs);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(Context ctx) {
		ConstructManager.INSTANCE.readFromPacket(this);
//		ConstructPreviewRenderer.reload();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.SYNC_CONSTRUCTS;
	}
}
