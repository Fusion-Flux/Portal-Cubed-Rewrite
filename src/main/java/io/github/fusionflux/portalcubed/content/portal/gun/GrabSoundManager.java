package io.github.fusionflux.portalcubed.content.portal.gun;

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
import net.minecraft.world.item.ItemStack;

public class GrabSoundManager {
	// magic number of ticks between the starts of the grab and hold sounds
	public static final int MAGIC_HOLD_DELAY = 28;

	private final Player player;

	@Environment(EnvType.CLIENT)
	private FollowingSoundInstance grabSound;
	@Environment(EnvType.CLIENT)
	private FollowingSoundInstance holdSound;

	private int grabTimer;

	public GrabSoundManager(Player player) {
		this.player = player;
	}

	public void onMainHandChange(ItemStack oldStack, ItemStack newStack) {
		if (!newStack.is(PortalCubedItems.PORTAL_GUN)) {
			// unequipped portal gun
			this.stop();
		} else if (!oldStack.is(PortalCubedItems.PORTAL_GUN)) {
			// equipped portal gun
			this.startHold();
		}
	}

	public void onHeldEntityChange(@Nullable HoldableEntity held) {
		if (held == null) {
			if (this.isActive()) {
				this.stop();
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
	}

	private boolean isActive() {
		return this.grabSound != null || this.holdSound != null;
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
		this.player.playSound(PortalCubedSounds.PORTAL_GUN_DROP, 1, 1);
	}

	private FollowingSoundInstance startPlaying(SoundEvent sound, boolean loop) {
		FollowingSoundInstance instance = new FollowingSoundInstance(sound, this.player.getSoundSource(), this.player);
		instance.setLooping(loop);
		Minecraft.getInstance().getSoundManager().play(instance);
		return instance;
	}
}
