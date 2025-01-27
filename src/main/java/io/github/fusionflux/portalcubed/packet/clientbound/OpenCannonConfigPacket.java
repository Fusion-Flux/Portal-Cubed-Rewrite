package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record OpenCannonConfigPacket(InteractionHand hand) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, OpenCannonConfigPacket> CODEC = StreamCodec.composite(
			PortalCubedStreamCodecs.HAND, OpenCannonConfigPacket::hand,
			OpenCannonConfigPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.OPEN_CANNON_CONFIG;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		ItemStack stack = ctx.player().getItemInHand(this.hand);
		if (stack.is(PortalCubedItems.CONSTRUCTION_CANNON)) {
			CannonSettings settings = ConstructionCannonItem.getCannonSettings(stack);
			if (settings == null)
				settings = CannonSettings.DEFAULT;
			Minecraft.getInstance().setScreen(new ConstructionCannonScreen(this.hand, settings));
		}
	}
}
