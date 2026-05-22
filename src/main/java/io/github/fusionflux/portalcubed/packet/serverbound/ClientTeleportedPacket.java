package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ClientTeleportedPacket(PortalPath.Serialized path) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, ClientTeleportedPacket> CODEC = PortalPath.Serialized.STREAM_CODEC.map(
			ClientTeleportedPacket::new, ClientTeleportedPacket::path
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CLIENT_TELEPORTED;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		TeleportTracker.getOrThrow(player).addTeleports(this.path);
	}

	public static ClientTeleportedPacket of(PortalPath path) {
		return new ClientTeleportedPacket(path.serialize());
	}
}
