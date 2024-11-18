package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

/**
 * Teleports need to be assumed to be local, because they're used for two completely different cases.
 * They're sent both to notify the client of an actual teleport and to ensure the client is accurate to the server.
 * The latter occurs all the time, the former is pretty much only /tp and ender pearls in vanilla.
 * Teleport packets are wrapped in this class when it's known that they aren't local.
 * TP packets are assumed to be non-local unless set otherwise by ServerEntityMixin for compatibility.
 */
public record LocalTeleportPacket(ClientboundTeleportEntityPacket wrapped) implements ClientboundPacket {
	public LocalTeleportPacket(ClientboundTeleportEntityPacket wrapped) {
		this.wrapped = wrapped;
		this.wrapped.pc$setLocal(true);
	}

	public LocalTeleportPacket(FriendlyByteBuf buf) {
		this(new ClientboundTeleportEntityPacket(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		this.wrapped.write(buf);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.LOCAL_TELEPORT;
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		this.wrapped.handle(player.connection);
	}
}
