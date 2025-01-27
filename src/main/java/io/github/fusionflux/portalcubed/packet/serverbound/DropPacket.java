package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DropPacket() implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, DropPacket> CODEC = StreamCodec.unit(new DropPacket());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.DROP;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		HoldableEntity held = ctx.player().getHeldEntity();
		if (held != null)
			held.drop();
	}
}
