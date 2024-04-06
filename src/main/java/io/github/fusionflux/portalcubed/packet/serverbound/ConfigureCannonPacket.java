package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.item.ItemStack;

import org.quiltmc.qsl.networking.api.PacketSender;

public record ConfigureCannonPacket(InteractionHand hand, CannonSettings settings) implements ServerboundPacket {
	public ConfigureCannonPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), buf.readJsonWithCodec(CannonSettings.CODEC));
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		ItemStack stack = player.getItemInHand(this.hand);
		if (stack.is(PortalCubedItems.CONSTRUCTION_CANNON)) {
			ConstructionCannonItem.setCannonSettings(stack, this.settings);
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		buf.writeJsonWithCodec(CannonSettings.CODEC, this.settings);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.CONFIGURE_CANNON;
	}
}
