package io.github.fusionflux.portalcubed.packet.serverbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportInfo;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record ClientTeleportedPacket(PortalTeleportInfo info, Vec3 pos, float xRot, float yRot) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, ClientTeleportedPacket> CODEC = StreamCodec.composite(
			PortalTeleportInfo.STREAM_CODEC, ClientTeleportedPacket::info,
			Vec3.STREAM_CODEC, ClientTeleportedPacket::pos,
			ByteBufCodecs.FLOAT, ClientTeleportedPacket::xRot,
			ByteBufCodecs.FLOAT, ClientTeleportedPacket::yRot,
			ClientTeleportedPacket::new
	);
	public static final double GLIDING_MAX = Math.sqrt(300);
	public static final double NORMAL_MAX = Math.sqrt(100);

	private static final Logger logger = LoggerFactory.getLogger(ClientTeleportedPacket.class);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CLIENT_TELEPORTED;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();

		if (this.isTeleportInvalid(player)) {
			logger.warn("Player attempted an invalid teleport: {}", player.getName());
			player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			return;
		}

		player.absMoveTo(this.pos.x, this.pos.y, this.pos.z, this.yRot, this.xRot);
		player.serverLevel().getChunkSource().move(player);
	}

	private boolean isTeleportInvalid(ServerPlayer player) {
		PortalManager manager = player.level().portalManager();
		double expectedDistance = getExpectedDistance(player);
		double distance = 0;

		// entrance
		PortalPair firstPair = manager.getPair(this.info.pairKey());
		if (firstPair == null || !firstPair.isLinked())
			return true;

		PortalInstance firstEntered = firstPair.getOrThrow(this.info.entered());
		Vec3 center = PortalTeleportHandler.centerOf(player);
		distance += (center.distanceTo(firstEntered.data.origin()));
		if (distance > expectedDistance)
			return true;

		// intermediate
		PortalInstance exited = firstPair.getOrThrow(this.info.entered().opposite());
		PortalTeleportInfo info = this.info.next();
		while (info != null) {
			PortalPair pair = manager.getPair(info.pairKey());
			if (pair == null || !pair.isLinked())
				return true;

			PortalInstance entered = pair.getOrThrow(info.entered());
			distance += (exited.data.origin().distanceTo(entered.data.origin()));
			if (distance > expectedDistance)
				return true;

			exited = pair.getOrThrow(info.entered().opposite());
			info = info.next();
		}

		// exit
		// info is now the last one
		Vec3 posToCenter = player.position().vectorTo(center);
		Vec3 finalCenter = this.pos.add(posToCenter);
		distance += (finalCenter.distanceTo(exited.data.origin()));
		return distance > expectedDistance;
	}

	private static double getExpectedDistance(ServerPlayer player) {
		/*
		magic numbers, based on ServerGamePacketListenerImpl's "moved too quickly" check.
		cleaned up version of the original:
		Vec3 newPos = ...;
		double dx = newPos.x - player.getX();
		double dy = newPos.y - player.getY();
		double dz = newPos.z - player.getZ();
		double deltaSqr = dx * dx + dy * dy + dz * dz;
		float maxDistSqr = player.isFallFlying() ? 300 : 100;
		double velSqr = player.getDeltaMovement().lengthSqr();
		if (deltaSqr - velSqr > maxDistSqr) {...} // too quick
		 */
		return player.isFallFlying() ? GLIDING_MAX : NORMAL_MAX;
	}
}
