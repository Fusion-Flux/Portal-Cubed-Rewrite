package io.github.fusionflux.portalcubed.packet.serverbound;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.misc.CrowbarItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public record CrowbarSwingPacket(Optional<BlockHitResult> hit, boolean didSwingAnim) implements ServerboundPacket {
	public static final StreamCodec<ByteBuf, CrowbarSwingPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(BlockHitResult.STREAM_CODEC), CrowbarSwingPacket::hit,
			ByteBufCodecs.BOOL, CrowbarSwingPacket::didSwingAnim,
			CrowbarSwingPacket::new
	);

	public CrowbarSwingPacket(@Nullable HitResult hit, boolean didSwingAnim) {
		this(hit instanceof BlockHitResult blockHit ? Optional.of(blockHit) : Optional.empty(), didSwingAnim);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CROWBAR_SWING;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		Player player = ctx.player();
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (stack.getItem() instanceof CrowbarItem crowbar && this.isHitValid(player)) {
			crowbar.onSwing(player, stack, this.hit.orElse(null), this.didSwingAnim);
		}
	}

	private boolean isHitValid(Player player) {
		if (this.hit.isEmpty())
			return true;

		return player.isWithinBlockInteractionRange(this.hit.get().getBlockPos(), Container.DEFAULT_DISTANCE_BUFFER);
	}
}
