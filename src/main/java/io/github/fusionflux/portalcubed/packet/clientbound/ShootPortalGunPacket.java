package io.github.fusionflux.portalcubed.packet.clientbound;

import java.util.UUID;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ShootPortalGunPacket(UUID player, Polarity polarity) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, ShootPortalGunPacket> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ShootPortalGunPacket::player,
			Polarity.STREAM_CODEC, ShootPortalGunPacket::polarity,
			ShootPortalGunPacket::new
	);

	public ShootPortalGunPacket(Player player, Polarity polarity) {
		this(player.getUUID(), polarity);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.SHOOT_PORTAL_GUN;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(ClientPlayNetworking.Context ctx) {
		Player player = ctx.player().clientLevel.getPlayerByUUID(this.player);
		if (player != null) {
			ItemStack heldItemStack = player.getMainHandItem();
			if (heldItemStack.getItem() instanceof PortalGunItem portalGun) {
				PortalGunSettings settings = PortalGunSettings.getOrDefault(heldItemStack);
				portalGun.doClientShootEffects(player, this.polarity, settings);
			}
		}
	}
}
