package io.github.fusionflux.portalcubed.packet.serverbound;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record DirectClickItemPacket(boolean attack, InteractionHand hand) implements ServerboundPacket {
	public DirectClickItemPacket(FriendlyByteBuf buf) {
		this(buf.readBoolean(), buf.readEnum(InteractionHand.class));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(attack);
		buf.writeEnum(hand);
	}

	@Override
	public ResourceLocation id() {
		return PortalCubedPackets.DIRECT_CLICK_ITEM;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() instanceof DirectClickItem direct) {
			if (this.attack) {
				direct.onAttack(player.level(), player, stack);
			} else {
				direct.onUse(player.level(), player, stack, hand);
			}
		}
	}
}
