package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
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

public record DisintegratePacket(int entity, int ticks) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, DisintegratePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, DisintegratePacket::entity,
			ByteBufCodecs.VAR_INT, DisintegratePacket::ticks,
			DisintegratePacket::new
	);

	public DisintegratePacket(Entity entity) {
		int ticks = Disintegration.remainingDisintegrationTicks(entity);
		if (ticks <= 0) {
			throw new IllegalArgumentException("Entity is not disintegrating: " + entity);
		}

		this(entity.getId(), ticks);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.DISINTEGRATE;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context ctx) {
		Entity entity = ctx.player().level().getEntity(this.entity);
		if (entity != null) {
			Disintegration.handleSync(entity, this.ticks);
		}
	}
}
