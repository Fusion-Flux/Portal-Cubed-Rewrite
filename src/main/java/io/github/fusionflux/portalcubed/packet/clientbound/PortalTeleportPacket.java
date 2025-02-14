package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.sync.TrackedTeleport;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

public record PortalTeleportPacket(int entityId, List<TrackedTeleport> teleports) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, PortalTeleportPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PortalTeleportPacket::entityId,
			TrackedTeleport.CODEC.apply(ByteBufCodecs.list()), PortalTeleportPacket::teleports,
			PortalTeleportPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTAL_TELEPORT;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context ctx) {
		AbstractClientPlayer player = ctx.player();
		Entity entity = player.clientLevel.getEntity(this.entityId);
		if (entity != null) {
			entity.getTeleportProgressTracker().addTeleports(this.teleports);
		} else {
			PortalCubed.LOGGER.warn("Received portal teleport for unknown entity: {}", this.entityId);
		}
	}
}
