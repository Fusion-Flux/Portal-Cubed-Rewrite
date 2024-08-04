package io.github.fusionflux.portalcubed.packet.clientbound;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlockEntity;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonConfigScreen;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenPedestalButtonConfigPacket(BlockPos pedestalButtonPos) implements ClientboundPacket {
	public OpenPedestalButtonConfigPacket(FriendlyByteBuf buf) {
		this(buf.readBlockPos());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pedestalButtonPos);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.OPEN_PEDESTAL_BUTTON_CONFIG;
	}

	@ClientOnly
	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		Minecraft client = Minecraft.getInstance();
		if (client.screen == null && player.level().getBlockEntity(pedestalButtonPos) instanceof PedestalButtonBlockEntity pedestalButton)
			client.setScreen(new PedestalButtonConfigScreen(pedestalButton));
	}
}
