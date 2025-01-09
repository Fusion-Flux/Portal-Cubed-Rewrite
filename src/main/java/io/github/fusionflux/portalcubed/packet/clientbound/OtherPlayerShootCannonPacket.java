package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.UUID;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record OtherPlayerShootCannonPacket(UUID player) implements ClientboundPacket {
	public OtherPlayerShootCannonPacket(Player player) {
		this(player.getUUID());
	}

	public OtherPlayerShootCannonPacket(FriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(this.player);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.SHOOT_CANNON_OTHER;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer localPlayer, PacketSender<CustomPacketPayload> responder) {
		Player player = localPlayer.clientLevel.getPlayerByUUID(this.player);
		if (player != null) {
			ShootCannonPacket.spawnParticlesForPlayer(player);
		}
	}
}
