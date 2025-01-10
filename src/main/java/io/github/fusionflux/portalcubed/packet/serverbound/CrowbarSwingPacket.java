package io.github.fusionflux.portalcubed.packet.serverbound;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.misc.CrowbarItem;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public record CrowbarSwingPacket(@Nullable BlockHitResult hit, boolean didSwingAnim) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, CrowbarSwingPacket> CODEC = StreamCodec.composite(
			PortalCubedStreamCodecs.nullable(PortalCubedStreamCodecs.BLOCK_HIT_RESULT), CrowbarSwingPacket::hit,
			ByteBufCodecs.BOOL, CrowbarSwingPacket::didSwingAnim,
			CrowbarSwingPacket::new
	);

	public CrowbarSwingPacket(@Nullable HitResult hit, boolean didSwingAnim) {
		this(hit instanceof BlockHitResult blockHit ? blockHit : null, didSwingAnim);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CROWBAR_SWING;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		Player player = ctx.player();
		ItemStack hand = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (hand.getItem() instanceof CrowbarItem crowbar && (hit == null || player.getEyePosition().distanceToSqr(this.hit.getLocation()) <= ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE))
			crowbar.onSwing(player, this.hit, this.didSwingAnim);
	}
}
