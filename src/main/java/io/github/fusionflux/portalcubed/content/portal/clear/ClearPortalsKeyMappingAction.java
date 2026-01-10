package io.github.fusionflux.portalcubed.content.portal.clear;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.framework.key.KeyMappingAction;
import io.github.fusionflux.portalcubed.framework.util.Or;
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
		// keep track of the pairs which have been cleared to avoid duplicate sounds
		Set<String> clearedPairs = new HashSet<>();

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			PortalGunSettings settings = held.get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
			if (settings == null)
				continue;

			for (PortalSettings portalSettings : Or.iterate(settings.portals())) {
				String key = portalSettings.pairFor(player);
				if (clearedPairs.contains(key))
					continue;

				// only play the sound if there's actually portals to remove
				if (player.clientLevel.portalManager().getPair(key) == null)
					continue;

				PortalGunSkin skin = settings.skin();
				if (skin == null)
					continue;

				clearedPairs.add(key);
				skin.sounds().fizzle().ifPresent(holder -> player.playSound(holder.value()));
			}
		}

		ClientPlayNetworking.send(ClearPortalsPacket.INSTANCE);
	}
}
