package io.github.fusionflux.portalcubed.packet.serverbound;

import org.jetbrains.annotations.Nullable;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public record DirectClickItemPacket(boolean attack, InteractionHand hand, @Nullable HitResult hit) implements ServerboundPacket {
	public DirectClickItemPacket(FriendlyByteBuf buf) {
		this(buf.readBoolean(), buf.readEnum(InteractionHand.class), buf.readNullable(FriendlyByteBuf::readBlockHitResult));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(attack);
		buf.writeEnum(hand);
		buf.writeNullable(this.hit instanceof BlockHitResult blockHit ? blockHit : null, FriendlyByteBuf::writeBlockHitResult);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.DIRECT_CLICK_ITEM;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() instanceof DirectClickItem direct) {
			if (this.attack) {
				direct.onAttack(player.level(), player, stack, hit);
			} else {
				direct.onUse(player.level(), player, stack, hit, hand);
			}
		}
	}
}
