package io.github.fusionflux.portalcubed.packet.serverbound;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ClientTeleportedPacket(Teleport teleport, Vec3 pos, float xRot, float yRot) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, ClientTeleportedPacket> CODEC = StreamCodec.composite(
			Teleport.CODEC, ClientTeleportedPacket::teleport,
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

		ServerPortalManager manager = player.serverLevel().portalManager();
		Teleport teleport = this.teleport;
		while (teleport != null) {
			// these are guaranteed to exist by isTeleportInvalid
			PortalReference entered = manager.getPortalOrThrow(teleport.entered);
			PortalReference exited = entered.opposite().orElseThrow();

			PortalCubedCriteriaTriggers.ENTER_PORTAL.trigger(player, entered);
			PortalCubedCriteriaTriggers.ENTER_PORTAL.trigger(player, exited);

			teleport = teleport.next.orElse(null);
		}
	}

	private boolean isTeleportInvalid(ServerPlayer player) {
		PortalManager manager = player.level().portalManager();
		double expectedDistance = getExpectedDistance(player);
		double distance = 0;

		// entrance
		PortalReference firstEntered = manager.getPortal(this.teleport.entered);
		if (firstEntered == null)
			return true;

		Optional<PortalReference> firstExited = firstEntered.opposite();
		if (firstExited.isEmpty())
			return true;

		Vec3 center = PortalTeleportHandler.centerOf(player);
		distance += (center.distanceTo(firstEntered.get().data.origin()));
		if (distance > expectedDistance)
			return true;

		// intermediate
		Portal exited = firstExited.get().get();
		Optional<Teleport> maybeNext = this.teleport.next;
		while (maybeNext.isPresent()) {
			Teleport next = maybeNext.get();
			PortalReference entered = manager.getPortal(next.entered);
			if (entered == null)
				return true;

			Optional<PortalReference> opposite = entered.opposite();
			if (opposite.isEmpty())
				return true;

			distance += (exited.data.origin().distanceTo(entered.get().data.origin()));
			if (distance > expectedDistance)
				return true;

			exited = opposite.get().get();
			maybeNext = next.next;
		}

		// exit
		// info is now the last one
		Vec3 posToCenter = player.position().vectorTo(center);
		Vec3 finalCenter = this.pos.add(posToCenter);
		distance += (finalCenter.distanceTo(exited.data.origin()));
		return distance > expectedDistance;
	}

	public static ClientTeleportedPacket of(Player player, PortalHitResult result) {
		Teleport first = Teleport.of(result);
		return new ClientTeleportedPacket(first, player.position(), player.getXRot(), player.getYRot());
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

	private record Teleport(PortalId entered, Optional<Teleport> next) {
		private static final StreamCodec<ByteBuf, Teleport> CODEC = StreamCodec.recursive(self -> StreamCodec.composite(
				PortalId.STREAM_CODEC, Teleport::entered,
				ByteBufCodecs.optional(self), Teleport::next,
				Teleport::new
		));

		private static Teleport of(PortalHitResult result) {
			return new Teleport(
					result.enteredPortal().id,
					result instanceof PortalHitResult.Mid mid ? Optional.of(Teleport.of(mid.next())) : Optional.empty()
			);
		}
	}
}
