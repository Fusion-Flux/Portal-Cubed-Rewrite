package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.OptionalInt;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record HoldStatusPacket(int holder, OptionalInt held) implements ClientboundPacket {
	public HoldStatusPacket(ServerPlayer holder, @Nullable HoldableEntity held) {
		this(holder.getId(), held == null ? OptionalInt.empty() : OptionalInt.of(held.getId()));
	}

	public HoldStatusPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt(), PacketUtils.readOptionalInt(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.holder);
		PacketUtils.writeOptionalInt(buf, this.held);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.HOLD_STATUS;
	}

	@ClientOnly
	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ClientLevel level = player.clientLevel;
		if (level.getEntity(this.holder) instanceof PlayerExt ext) {
			ext.pc$setHeldProp(this.held);
		}
	}
}
