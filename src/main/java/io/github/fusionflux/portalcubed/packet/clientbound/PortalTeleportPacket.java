package io.github.fusionflux.portalcubed.packet.clientbound;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.sync.tracker.TeleportTracker;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record PortalTeleportPacket(int entityId, PortalPath.Serialized path) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, PortalTeleportPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PortalTeleportPacket::entityId,
			PortalPath.Serialized.STREAM_CODEC, PortalTeleportPacket::path,
			PortalTeleportPacket::new
	);

	private static final Logger logger = LogUtils.getLogger();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTAL_TELEPORT;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context ctx) {
		Entity entity = ctx.player().clientLevel.getEntity(this.entityId);
		if (entity == null) {
			logger.warn("Ignoring portal teleport for unknown entity: {}", this.entityId);
			return;
		}

		if (entity instanceof Player player && player.isLocalPlayer()) {
			logger.warn("Ignoring portal teleport for local player");
			return;
		}

		TeleportTracker.getOrThrow(entity).addTeleports(this.path);
	}

	public static PortalTeleportPacket of(Entity entity, PortalPath path) {
		return new PortalTeleportPacket(entity.getId(), path.serialize());
	}
}
