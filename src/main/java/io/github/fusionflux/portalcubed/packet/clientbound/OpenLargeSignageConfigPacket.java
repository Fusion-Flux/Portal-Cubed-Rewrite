package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignagePanelBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignagePanelScreen;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public record OpenLargeSignageConfigPacket(BlockPos largeSignagePos) implements ClientboundPacket {
	public OpenLargeSignageConfigPacket(FriendlyByteBuf buf) {
		this(buf.readBlockPos());
	}
	@ClientOnly
	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		var client = Minecraft.getInstance();
		if (client.screen == null && HammerItem.usingHammer(player) && client.level.getBlockEntity(largeSignagePos) instanceof LargeSignagePanelBlockEntity largeSignagePanel) {
			client.setScreen(new LargeSignagePanelScreen(largeSignagePanel));
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(largeSignagePos);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.OPEN_LARGE_SIGNAGE_PANEL_CONFIG;
	}
}
