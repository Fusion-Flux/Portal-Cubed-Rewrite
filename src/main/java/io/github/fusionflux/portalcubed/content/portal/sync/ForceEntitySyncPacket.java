package io.github.fusionflux.portalcubed.content.portal.sync;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.world.entity.Entity;

public record ForceEntitySyncPacket(ClientboundEntityPositionSyncPacket wrapped) implements ClientboundPacket {
	public static final StreamCodec<FriendlyByteBuf, ForceEntitySyncPacket> CODEC = ClientboundEntityPositionSyncPacket.STREAM_CODEC.map(
			ForceEntitySyncPacket::new, ForceEntitySyncPacket::wrapped
	);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context ctx) {
		int id = this.wrapped.id();
		Entity entity = ctx.player().clientLevel.getEntity(id);

		if (entity == null) {
			PortalCubed.LOGGER.error("Ignoring forced sync for missing entity {}", id);
			return;
		}

		TeleportTracker.of(entity).ifPresent(TeleportTracker::abort);
		this.wrapped.handle(ctx.player().connection);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.FORCE_ENTITY_SYNC;
	}

	public static ForceEntitySyncPacket of(Entity entity) {
		return new ForceEntitySyncPacket(ClientboundEntityPositionSyncPacket.of(entity));
	}
}
