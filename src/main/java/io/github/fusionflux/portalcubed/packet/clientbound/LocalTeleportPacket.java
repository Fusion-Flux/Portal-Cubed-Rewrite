package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;

/**
 * Teleports need to be assumed to be local, because they're used for two completely different cases.
 * They're sent both to notify the client of an actual teleport and to ensure the client is accurate to the server.
 * The latter occurs all the time, the former is pretty much only /tp and ender pearls in vanilla.
 * Teleport packets are wrapped in this class when it's known that they aren't local.
 * TP packets are assumed to be non-local unless set otherwise by ServerEntityMixin for compatibility.
 */
public record LocalTeleportPacket(ClientboundTeleportEntityPacket wrapped) implements ClientboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, LocalTeleportPacket> CODEC = StreamCodec.composite(
			ClientboundTeleportEntityPacket.STREAM_CODEC, LocalTeleportPacket::wrapped,
			LocalTeleportPacket::new
	);

	public LocalTeleportPacket(ClientboundTeleportEntityPacket wrapped) {
		this.wrapped = wrapped;
		this.wrapped.pc$setLocal(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.LOCAL_TELEPORT;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		this.wrapped.handle(ctx.player().connection);
	}
}
