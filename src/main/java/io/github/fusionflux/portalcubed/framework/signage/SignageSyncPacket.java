package io.github.fusionflux.portalcubed.framework.signage;

import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.qsl.networking.api.PacketSender;

import java.util.Map;

public record SignageSyncPacket(Map<ResourceLocation, Signage> entries) implements ClientboundPacket {
	public SignageSyncPacket(FriendlyByteBuf buf) {
		this(buf.readMap(FriendlyByteBuf::readResourceLocation, $ -> buf.readJsonWithCodec(Signage.CODEC)));
	}

	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		SignageManager.INSTANCE.readFromPacket(this);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.entries, FriendlyByteBuf::writeResourceLocation, ($, value) -> buf.writeJsonWithCodec(Signage.CODEC, value));
	}

	@Override
	public ResourceLocation getId() {
		return null;
	}
}
