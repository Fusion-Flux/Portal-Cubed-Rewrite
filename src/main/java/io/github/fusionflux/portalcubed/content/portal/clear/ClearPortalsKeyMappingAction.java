package io.github.fusionflux.portalcubed.content.portal.clear;

import java.util.Objects;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.framework.key.KeyMappingAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClearPortalsKeyMappingAction implements KeyMappingAction {
	@Override
	public void onPressed(Minecraft mc) {
		LocalPlayer player = Objects.requireNonNull(mc.player);

		// packet will handle removal on the server, just play fizzle sounds here
		// keep track of the first cleared key to avoid a duplicate sound
		String clearedKey = null;

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			PortalGunSettings settings = held.get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
			if (settings == null)
				continue;

			String key = settings.pairFor(player);
			if (Objects.equals(key, clearedKey))
				continue;

			// only play the sound if there's actually portals to remove
			if (player.clientLevel.portalManager().getPair(key) == null)
				continue;

			PortalGunSkin skin = settings.skin();
			if (skin == null)
				continue;

			clearedKey = key;

			skin.sounds().fizzle().ifPresent(holder -> player.playSound(holder.value()));
		}

		ClientPlayNetworking.send(ClearPortalsPacket.INSTANCE);
	}
}
