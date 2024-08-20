package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.content.crowbar.CrowbarItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketSender;

public record CrowbarSwingPacket(@Nullable HitResult hit, boolean didSwingAnim) implements ServerboundPacket {
	public CrowbarSwingPacket(FriendlyByteBuf buf) {
		this(buf.readNullable(FriendlyByteBuf::readBlockHitResult), buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeNullable(this.hit instanceof BlockHitResult blockHit ? blockHit : null, FriendlyByteBuf::writeBlockHitResult);
		buf.writeBoolean(this.didSwingAnim);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.CROWBAR_SWING;
	}

	@Override
	public void handle(ServerPlayer player, PacketSender<CustomPacketPayload> responder) {
		ItemStack hand = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (hand.getItem() instanceof CrowbarItem crowbar && (hit == null || player.getEyePosition().distanceToSqr(this.hit.getLocation()) <= ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE))
			crowbar.onSwing(player, this.hit, this.didSwingAnim);
	}
}
