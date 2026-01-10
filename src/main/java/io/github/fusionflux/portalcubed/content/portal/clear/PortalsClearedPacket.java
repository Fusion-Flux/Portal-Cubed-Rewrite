package io.github.fusionflux.portalcubed.content.portal.clear;

import java.util.Set;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkinManager;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record PortalsClearedPacket(Set<ResourceKey<PortalGunSkin>> skins) implements ClientboundPacket {
	public static final StreamCodec<ByteBuf, PortalsClearedPacket> CODEC = PortalCubedStreamCodecs.set(
			PortalGunSkin.RESOURCE_KEY_STREAM_CODEC
	).map(PortalsClearedPacket::new, PortalsClearedPacket::skins);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTALS_CLEARED;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context ctx) {
		LocalPlayer player = ctx.player();

		for (ResourceKey<PortalGunSkin> key : this.skins) {
			PortalGunSkin skin = PortalGunSkinManager.INSTANCE.get(key);

			if (skin != null) {
				skin.sounds().fizzle().ifPresent(sound -> player.playSound(sound.value()));
			} else {
				PortalCubed.LOGGER.error("Unknown portal gun skin, can't play fizzle sound: {}", key.location());
			}
		}
	}
}
