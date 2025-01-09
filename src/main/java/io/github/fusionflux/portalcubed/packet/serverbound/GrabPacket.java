package io.github.fusionflux.portalcubed.packet.serverbound;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record GrabPacket(int grabbed) implements ServerboundPacket {
	public GrabPacket(HoldableEntity grabbed) {
		this(grabbed.getId());
	}

	public GrabPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.grabbed);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.GRAB;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		ServerLevel level = player.serverLevel();
		Entity entity = level.getEntity(this.grabbed);
		if (entity instanceof HoldableEntity holdable) {
			if (!player.pc$disintegrating() && entity.position().distanceToSqr(player.getEyePosition()) < HoldableEntity.MAX_DIST_SQR) {
				holdable.grab(player);
			}
		}
	}
}
