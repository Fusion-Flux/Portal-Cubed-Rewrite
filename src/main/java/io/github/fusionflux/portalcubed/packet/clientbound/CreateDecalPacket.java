package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.framework.particle.DecalPos;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record CreateDecalPacket(ParticleOptions options, DecalPos pos) implements ClientboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, CreateDecalPacket> CODEC = StreamCodec.composite(
			ParticleTypes.STREAM_CODEC, CreateDecalPacket::options,
			DecalPos.STREAM_CODEC, CreateDecalPacket::pos,
			CreateDecalPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CREATE_DECAL;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		Vec3 pos = this.pos.resolve();
		// encode the normal into the velocity fields. this is bad but particles are bad in general
		Vec3i normal = this.pos.face().getStep();
		ctx.player().level().addParticle(this.options, pos.x, pos.y, pos.z, normal.getX(), normal.getY(), normal.getZ());
	}
}
