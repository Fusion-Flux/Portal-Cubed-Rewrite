package io.github.fusionflux.portalcubed.framework.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class FollowingSoundInstance extends AbstractTickableSoundInstance {
	protected final Entity followed;
	protected final boolean stopWhenSilent;

	public FollowingSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Entity followed) {
		this(soundEvent, soundSource, followed, true);
	}

	public FollowingSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Entity followed, boolean stopWhenSilent) {
		super(soundEvent, soundSource, followed.getRandom());
		this.followed = followed;
		this.stopWhenSilent = stopWhenSilent;
		this.x = followed.getX();
		this.y = followed.getY();
		this.z = followed.getZ();
		this.delay = 0;
	}

	protected boolean shouldStop() {
		if (Minecraft.getInstance().player == null)
			return true;

		if (this.followed.isRemoved() || this.followed.isSilent())
			return this.stopWhenSilent;

		return false;
	}

	@Override
	public void tick() {
		if (this.shouldStop()) {
			this.stop();
			return;
		}

		this.x = this.followed.getX();
		this.y = this.followed.getY();
		this.z = this.followed.getZ();
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public void forceStop() {
		this.stop();
	}
}
