package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportInfo;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import org.quiltmc.qsl.networking.api.PacketSender;

public record ClientTeleportedPacket(PortalTeleportInfo info, Vec3 pos, float xRot, float yRot) implements ServerboundPacket {
	public static final double GLIDING_MAX = Math.sqrt(300);
	public static final double NORMAL_MAX = Math.sqrt(100);

	public ClientTeleportedPacket(FriendlyByteBuf buf) {
		this(PortalTeleportInfo.fromNetwork(buf), buf.readVec3(), buf.readFloat(), buf.readFloat());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		this.info.toNetwork(buf);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.CLIENT_TELEPORTED;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		if (this.isTeleportInvalid(player)) {
			PortalCubed.LOGGER.warn("Player attempted an invalid teleport: {}", player.getName());
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
		PortalPair firstPair = manager.getPair(this.info.pairId());
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
			PortalPair pair = manager.getPair(info.pairId());
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
