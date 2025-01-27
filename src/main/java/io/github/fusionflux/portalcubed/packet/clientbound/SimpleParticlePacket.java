package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SimpleParticlePacket(ParticleOptions options, double x, double y, double z, double velocityX, double velocityY, double velocityZ) implements ClientboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleParticlePacket> CODEC = StreamCodec.composite(
			ParticleTypes.STREAM_CODEC, SimpleParticlePacket::options,
			ByteBufCodecs.DOUBLE, SimpleParticlePacket::x,
			ByteBufCodecs.DOUBLE, SimpleParticlePacket::y,
			ByteBufCodecs.DOUBLE, SimpleParticlePacket::z,
			ByteBufCodecs.DOUBLE, SimpleParticlePacket::velocityX,
			ByteBufCodecs.DOUBLE, SimpleParticlePacket::velocityY,
			ByteBufCodecs.DOUBLE, SimpleParticlePacket::velocityZ,
			SimpleParticlePacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.SIMPLE_PARTICLE;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		ctx.player().clientLevel.addParticle(this.options, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
	}
}
