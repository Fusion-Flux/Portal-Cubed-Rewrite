package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.OptionalInt;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PropHoldPacket(int holderId, OptionalInt propId) implements ClientboundPacket {
	public PropHoldPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), PacketUtils.readOptionalInt(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(holderId);
		PacketUtils.writeOptionalInt(buf, propId);
	}

	@Override
	public ResourceLocation id() {
		return PortalCubedPackets.PROP_HOLD;
	}

	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		var level = player.level();
		if (level.getEntity(holderId) instanceof PlayerExt holder)
			holder.pc$heldProp(propId);
	}
}
