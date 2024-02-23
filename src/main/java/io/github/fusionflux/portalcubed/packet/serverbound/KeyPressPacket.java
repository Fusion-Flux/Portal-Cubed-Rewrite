package io.github.fusionflux.portalcubed.packet.serverbound;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.PortalCubedKeyBindings.KeyBinding;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record KeyPressPacket(KeyBinding key) implements ServerboundPacket {
	public KeyPressPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(KeyBinding.class));
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.KEY_PRESS;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(key);
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		key.onPress.accept(player);
	}
}
