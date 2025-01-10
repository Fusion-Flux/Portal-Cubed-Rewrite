package io.github.fusionflux.portalcubed.packet.serverbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record RequestEntitySyncPacket(int entityId) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, RequestEntitySyncPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, RequestEntitySyncPacket::entityId,
			RequestEntitySyncPacket::new
	);

	private static final Logger logger = LoggerFactory.getLogger(RequestEntitySyncPacket.class);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.REQUEST_ENTITY_SYNC;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		Level level = player.level();
		Entity entity = level.getEntity(this.entityId);
		if (entity == null) {
			logger.error("Player {} requested sync packet for unknown entity {}", player.getName(), this.entityId);
			return;
		}

		player.connection.send(new ClientboundTeleportEntityPacket(entity));
	}
}
