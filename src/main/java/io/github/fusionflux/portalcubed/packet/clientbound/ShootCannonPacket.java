package io.github.fusionflux.portalcubed.packet.clientbound;

import org.quiltmc.qsl.networking.api.PacketSender;

import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import io.github.fusionflux.portalcubed.framework.extension.ItemInHandRendererExt;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record ShootCannonPacket(InteractionHand hand) implements ClientboundPacket {
	public ShootCannonPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class));
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(hand);
	}

	@Override
	public ResourceLocation getId() {
		return PortalCubedPackets.SHOOT_CANNON;
	}

	@Override
	public void handle(LocalPlayer player, PacketSender<CustomPacketPayload> responder) {
		if (player.getItemInHand(hand).getItem() instanceof ConstructionCannonItem) {
			var itemInHandRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
			((ItemInHandRendererExt) itemInHandRenderer).pc$recoil();
		}
	}
}
