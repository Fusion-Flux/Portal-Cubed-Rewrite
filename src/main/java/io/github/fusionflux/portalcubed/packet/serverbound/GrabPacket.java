package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;

public record GrabPacket(int grabbed) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, GrabPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GrabPacket::grabbed,
			GrabPacket::new
	);

	public GrabPacket(HoldableEntity grabbed) {
		this(grabbed.getId());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.GRAB;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		ServerPlayer player = ctx.player();
		ServerLevel level = player.serverLevel();
		Entity entity = level.getEntity(this.grabbed);
		if (entity instanceof HoldableEntity holdable) {
			if (!player.pc$disintegrating() && player.canInteractWithEntity(entity, Container.DEFAULT_DISTANCE_BUFFER)) {
				holdable.grab(player);
			}
		}
	}
}
