package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.qsl.networking.api.PacketSender;

public record SimpleParticlePacket(ParticleOptions options, double x, double y, double z, double velocityX, double velocityY, double velocityZ) implements ClientboundPacket {
	public SimpleParticlePacket(FriendlyByteBuf buf) {
		this(PacketUtils.readParticleOptions(buf), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		PacketUtils.writeParticleOptions(buf, options);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeDouble(velocityX);
		buf.writeDouble(velocityY);
		buf.writeDouble(velocityZ);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.SIMPLE_PARTICLE;
	}

	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		player.clientLevel.addParticle(options, x, y, z, velocityX, velocityY, velocityZ);
	}
}
