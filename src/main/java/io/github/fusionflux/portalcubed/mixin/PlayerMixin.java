package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerMixin implements PlayerExt {
	@Unique
	@Nullable
	private HoldableEntity heldEntity;

	@ClientOnly
	@Unique
	private int grabSoundTimer = 0;
	@ClientOnly
	@Unique
	@Nullable
	private FollowingSoundInstance grabSound = null;
	@ClientOnly
	@Unique
	@Nullable
	private FollowingSoundInstance holdLoopSound = null;

	@Override
	public void setHeldEntity(@Nullable HoldableEntity heldEntity) {
		this.heldEntity = heldEntity;
	}

	@Override
	@Nullable
	public HoldableEntity getHeldEntity() {
		return this.heldEntity;
	}

	@Override
	public void pc$grabSoundTimer(int timer) {
		grabSoundTimer = timer;
	}

	@Override
	public int pc$grabSoundTimer() {
		return grabSoundTimer;
	}

	@Override
	public void pc$grabSound(Object grabSound) {
		this.grabSound = (FollowingSoundInstance) grabSound;
	}

	@Override
	public Object pc$grabSound() {
		return grabSound;
	}

	@Override
	public void pc$holdLoopSound(Object holdLoopSound) {
		this.holdLoopSound = (FollowingSoundInstance) holdLoopSound;
	}

	@Override
	public Object pc$holdLoopSound() {
		return holdLoopSound;
	}
}
