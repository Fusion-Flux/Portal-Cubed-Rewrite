package io.github.fusionflux.portalcubed;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedKeyBindings;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.portal.PortalRenderer;
import io.github.fusionflux.portalcubed.content.prop.PropModels;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import io.github.fusionflux.portalcubed.framework.model.PortalCubedModelLoadingPlugin;
import io.github.fusionflux.portalcubed.framework.model.emissive.EmissiveLoader;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.entity.event.api.client.ClientEntityTickCallback;

public class PortalCubedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		PortalRenderer.init();
		PortalCubedKeyBindings.init();

		PropModels.register();
		PreparableModelLoadingPlugin.register(EmissiveLoader.INSTANCE, PortalCubedModelLoadingPlugin.INSTANCE);

		ClientEntityTickCallback.EVENT.register((entity, isPassengerTick) -> {
			if (entity instanceof Player player) {
				boolean holdingPortalGun = player.getMainHandItem().is(PortalCubedItems.PORTAL_GUN);
				var ext = (PlayerExt) player;

				var soundManager = Minecraft.getInstance().getSoundManager();
				int grabSoundTimer = ext.pc$grabSoundTimer();
				var holdLoopSound = (FollowingSoundInstance) ext.pc$holdLoopSound();
				var grabSound = (FollowingSoundInstance) ext.pc$grabSound();

				if (grabSound != null) {
					grabSoundTimer--;
					ext.pc$grabSoundTimer(grabSoundTimer);

					if (!holdingPortalGun) {
						grabSound.forceStop();
						grabSound = null;
						ext.pc$grabSound(grabSound);
					} else if (grabSoundTimer <= 0) {
						grabSound = null;
						ext.pc$grabSound(grabSound);

						holdLoopSound = PortalCubedSounds.createPortalGunHoldLoop(player);
						soundManager.play(holdLoopSound);
						ext.pc$holdLoopSound(holdLoopSound);
					}
				}

				if (holdLoopSound != null && !holdingPortalGun) {
					holdLoopSound.forceStop();
					ext.pc$holdLoopSound(null);
				} else if (holdingPortalGun && (holdLoopSound == null && grabSound == null) && ext.pc$heldProp().isPresent()) {
					holdLoopSound = PortalCubedSounds.createPortalGunHoldLoop(player);
					soundManager.play(holdLoopSound);
					ext.pc$holdLoopSound(holdLoopSound);
				}
			}
		});
	}
}
