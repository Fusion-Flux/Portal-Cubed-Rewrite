package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportInfo;
import io.github.fusionflux.portalcubed.content.portal.TeleportProgressTracker;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
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

public record PortalTeleportPacket(int entityId, PortalTeleportInfo info) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, PortalTeleportPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PortalTeleportPacket::entityId,
			PortalTeleportInfo.STREAM_CODEC, PortalTeleportPacket::info,
			PortalTeleportPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTAL_TELEPORT;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		AbstractClientPlayer player = ctx.player();
		Entity entity = player.clientLevel.getEntity(this.entityId);
		if (entity == null) {
			PortalCubed.LOGGER.warn("Received portal teleport for unknown entity: {}", this.entityId);
			return;
		} else if (this.isInfoInvalid(player.clientLevel.portalManager())) {
			PortalCubed.LOGGER.warn("Received portal teleport containing unknown portals");
			return;
		}

		int timeoutAge = entity.tickCount + TeleportProgressTracker.TIMEOUT_TICKS;
		TeleportProgressTracker tracker = entity.getTeleportProgressTracker();
		if (tracker != null) {
			tracker.append(this.info, timeoutAge);
		} else {
			entity.setTeleportProgressTracker(new TeleportProgressTracker(timeoutAge, this.info));
		}
	}

	private boolean isInfoInvalid(PortalManager manager) {
		PortalTeleportInfo info = this.info;
		while (info != null) {
			if (manager.getPair(info.pairKey()) == null) {
				return true;
			}

			info = info.next();
		}

		return false;
	}
}
