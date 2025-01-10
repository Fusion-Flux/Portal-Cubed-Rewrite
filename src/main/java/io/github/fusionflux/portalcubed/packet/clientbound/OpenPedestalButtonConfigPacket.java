package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonConfigScreen;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenPedestalButtonConfigPacket(BlockPos pedestalButtonPos) implements ClientboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenPedestalButtonConfigPacket> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, OpenPedestalButtonConfigPacket::pedestalButtonPos,
			OpenPedestalButtonConfigPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.OPEN_PEDESTAL_BUTTON_CONFIG;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		Minecraft client = Minecraft.getInstance();
		if (client.screen == null && ctx.player().clientLevel.getBlockEntity(pedestalButtonPos) instanceof PedestalButtonBlockEntity pedestalButton)
			client.setScreen(new PedestalButtonConfigScreen(pedestalButton));
	}
}
