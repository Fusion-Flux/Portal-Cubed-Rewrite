package io.github.fusionflux.portalcubed.packet.serverbound;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ConfigureCannonPacket(InteractionHand hand, CannonSettings settings) implements ServerboundPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureCannonPacket> CODEC = StreamCodec.composite(
			PortalCubedStreamCodecs.HAND, ConfigureCannonPacket::hand,
			CannonSettings.STREAM_CODEC, ConfigureCannonPacket::settings,
			ConfigureCannonPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.CONFIGURE_CANNON;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		Player player = ctx.player();
		ItemStack stack = player.getItemInHand(this.hand);
		if (stack.is(PortalCubedItems.CONSTRUCTION_CANNON))
			ConstructionCannonItem.setCannonSettings(stack, this.settings);
	}
}
