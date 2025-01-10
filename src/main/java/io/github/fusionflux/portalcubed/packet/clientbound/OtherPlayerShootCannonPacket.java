package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.UUID;

import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record OtherPlayerShootCannonPacket(UUID player) implements ClientboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, OtherPlayerShootCannonPacket> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, OtherPlayerShootCannonPacket::player,
			OtherPlayerShootCannonPacket::new
	);

	public OtherPlayerShootCannonPacket(Player player) {
		this(player.getUUID());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.SHOOT_CANNON_OTHER;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		Player player = ctx.player().clientLevel.getPlayerByUUID(this.player);
		if (player != null) {
			ShootCannonPacket.spawnParticlesForPlayer(player);
		}
	}
}
