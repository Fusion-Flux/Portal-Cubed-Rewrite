package io.github.fusionflux.portalcubed.packet.serverbound;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.framework.item.AttackListeningItem;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public record CustomAttackPacket(InteractionHand hand, @Nullable BlockHitResult hit) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, CustomAttackPacket> CODEC = StreamCodec.composite(
			PortalCubedStreamCodecs.HAND, CustomAttackPacket::hand,
			PortalCubedStreamCodecs.nullable(PortalCubedStreamCodecs.BLOCK_HIT_RESULT), CustomAttackPacket::hit,
			CustomAttackPacket::new
	);

	public CustomAttackPacket(InteractionHand hand, @Nullable HitResult hit) {
		this(hand, hit instanceof BlockHitResult blockHit ? blockHit : null);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CUSTOM_ATTACK;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		Player player = ctx.player();
		ItemStack stack = player.getItemInHand(this.hand);
		if (stack.getItem() instanceof AttackListeningItem direct) {
			direct.onAttack(player.level(), player, stack, this.hit);
		}
	}
}
