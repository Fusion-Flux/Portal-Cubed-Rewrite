package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import org.quiltmc.qsl.networking.api.PacketSender;

public record RequestEntitySyncPacket(int entityId) implements ServerboundPacket {
	public RequestEntitySyncPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.entityId);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.REQUEST_ENTITY_SYNC;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		Level level = player.level();
		Entity entity = level.getEntity(this.entityId);
		if (entity == null) {
			PortalCubed.LOGGER.error("Player {} requested sync packet for unknown entity {}", player.getName(), this.entityId);
			return;
		}

		player.connection.send(new ClientboundTeleportEntityPacket(entity));
	}
}
