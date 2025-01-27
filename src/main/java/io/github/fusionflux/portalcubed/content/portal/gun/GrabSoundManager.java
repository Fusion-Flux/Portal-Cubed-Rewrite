package io.github.fusionflux.portalcubed.content.portal.gun;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public class GrabSoundManager {
	// magic number of ticks between the starts of the grab and hold sounds
	public static final int MAGIC_HOLD_DELAY = 28;

	private final Player player;

	@Environment(EnvType.CLIENT)
	private FollowingSoundInstance grabSound;
	@Environment(EnvType.CLIENT)
	private FollowingSoundInstance holdSound;

	private int grabTimer;
	private ItemVariant lastHeldItem;

	public GrabSoundManager(Player player) {
		this.player = player;
		this.lastHeldItem = this.findHeldItem();
	}

	public void onHeldItemChange(ItemVariant newItem) {
		if (isPortalGun(this.lastHeldItem) && !isPortalGun(newItem) && this.isActive()) {
			// unequipped portal gun
			this.drop();
		} else if (!isPortalGun(this.lastHeldItem) && isPortalGun(newItem) && this.playerIsHolding()) {
			// equipped portal gun
			this.startGrab();
		}

		this.lastHeldItem = newItem;
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

		ItemVariant held = this.findHeldItem();
		if (!this.lastHeldItem.equals(held)) {
			this.onHeldItemChange(held);
		}
	}

	private boolean isActive() {
		return this.grabSound != null || this.holdSound != null;
	}

	private boolean playerIsHolding() {
		return this.player.getHeldEntity() != null;
	}

	private ItemVariant findHeldItem() {
		return ItemVariant.of(this.player.getMainHandItem());
	}

	private void stop() {
		this.stopGrab();
		this.stopHold();
	}

	private void startGrab() {
		this.grabSound = this.startPlaying(PortalCubedSounds.PORTAL_GUN_GRAB, false);
		this.grabTimer = MAGIC_HOLD_DELAY;
	}

	private void stopGrab() {
		if (this.grabSound != null) {
			this.grabSound.forceStop();
			this.grabSound = null;
			this.grabTimer = 0;
		}
	}

	private void startHold() {
		this.holdSound = this.startPlaying(PortalCubedSounds.PORTAL_GUN_HOLD_LOOP, true);
	}

	private void stopHold() {
		if (this.holdSound != null) {
			this.holdSound.forceStop();
			this.holdSound = null;
		}
	}

	private void drop() {
		this.stop();
		this.player.playSound(PortalCubedSounds.PORTAL_GUN_DROP, 1, 1);
	}

	private FollowingSoundInstance startPlaying(SoundEvent sound, boolean loop) {
		FollowingSoundInstance instance = new FollowingSoundInstance(sound, this.player.getSoundSource(), this.player);
		instance.setLooping(loop);
		Minecraft.getInstance().getSoundManager().play(instance);
		return instance;
	}

	private static boolean isPortalGun(ItemVariant item) {
		return item.isOf(PortalCubedItems.PORTAL_GUN);
	}
}
