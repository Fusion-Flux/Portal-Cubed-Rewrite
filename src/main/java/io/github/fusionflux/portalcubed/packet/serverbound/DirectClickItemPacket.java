package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class DirectClickItemPacket implements ServerboundPacket {
	private final boolean attack;
	private final InteractionHand hand;

	public DirectClickItemPacket(boolean attack, InteractionHand hand) {
		this.attack = attack;
		this.hand = hand;
	}

	public DirectClickItemPacket(FriendlyByteBuf buf) {
		this.attack = buf.readBoolean();
		this.hand = buf.readEnum(InteractionHand.class);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(attack);
		buf.writeEnum(hand);
	}

	@Override
	public PacketType<?> getType() {
		return PortalCubedPackets.DIRECT_CLICK_ITEM;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender responder) {
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
