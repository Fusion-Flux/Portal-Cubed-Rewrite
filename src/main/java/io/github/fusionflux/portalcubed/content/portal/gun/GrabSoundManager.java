package io.github.fusionflux.portalcubed.content.portal.gun;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public class GrabSoundManager {
	// magic number of ticks between the starts of the grab and hold sounds
	private final Player player;

	@Environment(EnvType.CLIENT)
	private FollowingSoundInstance grabSoundPlaying;
	@Environment(EnvType.CLIENT)
	private FollowingSoundInstance holdSoundPlaying;

	private int grabTimer;
	private PortalGunSettings portalGun;

	public GrabSoundManager(Player player) {
		this.player = player;
		this.portalGun = this.findHeld();
	}

	public void onHeldItemChange(@Nullable PortalGunSettings newPortalGun) {
		PortalGunSettings oldPortalGun = this.portalGun;
		this.portalGun = newPortalGun;

		if (oldPortalGun != null && newPortalGun == null && this.isActive()) {
			// unequipped portal gun
			this.drop();
		} else if (oldPortalGun == null && newPortalGun != null && this.playerIsHolding()) {
			// equipped portal gun
			this.startGrab();
		}
	}

	public void onFailedGrab() {
		this.sounds().cannotGrab()
				.ifPresent(sound -> this.player.playSound(sound.value()));
	}

	public void onHeldEntityChange(@Nullable HoldableEntity held) {
		if (held == null) {
			if (this.isActive()) {
				this.drop();
			}
		} else {
			this.startGrab();
		}
	}

	public void tick() {
		if (this.grabTimer > 0) {
			this.grabTimer--;
			if (this.grabTimer == 0) {
				this.startHold();
			}
		}

		PortalGunSettings held = this.findHeld();
		if (this.portalGun == null || !this.portalGun.equals(held)) {
			this.onHeldItemChange(held);
		}
	}

	@Nullable
	private PortalGunSettings findHeld() {
		return this.player.getMainHandItem().get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
	}

	private boolean isActive() {
		return this.grabSoundPlaying != null || this.holdSoundPlaying != null;
	}

	private boolean playerIsHolding() {
		return this.player.getHeldEntity() != null;
	}

	private void stop() {
		this.stopGrab();
		this.stopHold();
	}

	private void startGrab() {
		this.sounds().grab().ifPresent(grab -> {
			this.grabSoundPlaying = this.startPlaying(grab.sound().value(), false);
			this.grabTimer = grab.lengthInTicks();
		});
	}

	private void stopGrab() {
		if (this.grabSoundPlaying != null) {
			this.grabSoundPlaying.forceStop();
			this.grabSoundPlaying = null;
			this.grabTimer = 0;
		}
	}

	private void startHold() {
		this.sounds().holdLoop()
				.ifPresent(sound -> this.holdSoundPlaying = this.startPlaying(sound.value(), true));
	}

	private void stopHold() {
		if (this.holdSoundPlaying != null) {
			this.holdSoundPlaying.forceStop();
			this.holdSoundPlaying = null;
		}
	}

	private void drop() {
		this.stop();
		this.sounds().release()
				.ifPresent(sound -> this.player.playSound(sound.value()));
	}

	private PortalGunSkin.Sounds sounds() {
		if (this.portalGun != null) {
			PortalGunSkin skin = this.portalGun.skin();
			if (skin != null)
				return skin.sounds();
		}
		return PortalGunSkin.Sounds.EMPTY;
	}

	private FollowingSoundInstance startPlaying(SoundEvent sound, boolean loop) {
		FollowingSoundInstance instance = new FollowingSoundInstance(sound, this.player.getSoundSource(), this.player);
		instance.setLooping(loop);
		Minecraft.getInstance().getSoundManager().play(instance);
		return instance;
	}
}
