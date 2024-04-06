package io.github.fusionflux.portalcubed.packet.clientbound;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.item.ItemStack;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;

public record OpenCannonConfigPacket(InteractionHand hand) implements ClientboundPacket {
	public OpenCannonConfigPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
	}

	@Override
	@ClientOnly
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		ItemStack stack = player.getItemInHand(this.hand);
		if (stack.is(PortalCubedItems.CONSTRUCTION_CANNON)) {
			CannonSettings settings = ConstructionCannonItem.getCannonSettings(stack);
			if (settings == null)
				settings = CannonSettings.DEFAULT;
			Minecraft.getInstance().setScreen(new ConstructionCannonScreen(this.hand, settings));
		}
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.OPEN_CANNON_CONFIG;
	}
}
