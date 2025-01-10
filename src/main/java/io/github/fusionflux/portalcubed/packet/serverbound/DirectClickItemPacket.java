package io.github.fusionflux.portalcubed.packet.serverbound;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.item.DirectClickItem;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public record DirectClickItemPacket(boolean attack, InteractionHand hand, @Nullable BlockHitResult hit) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, DirectClickItemPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, DirectClickItemPacket::attack,
			PortalCubedStreamCodecs.HAND, DirectClickItemPacket::hand,
			PortalCubedStreamCodecs.nullable(PortalCubedStreamCodecs.BLOCK_HIT_RESULT), DirectClickItemPacket::hit,
			DirectClickItemPacket::new
	);

	public DirectClickItemPacket(boolean attack, InteractionHand hand, @Nullable HitResult hit) {
		this(attack, hand, hit instanceof BlockHitResult blockHit ? blockHit : null);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.DIRECT_CLICK_ITEM;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		Player player = ctx.player();
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
