package io.github.fusionflux.portalcubed.framework.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class FollowingSoundInstance extends AbstractTickableSoundInstance {
	protected final Entity followed;
	protected final boolean respectSilence;

	public FollowingSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Entity followed) {
		this(soundEvent, soundSource, followed, true);
	}

	public FollowingSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Entity followed, boolean respectSilence) {
		super(soundEvent, soundSource, followed.level().random);
		this.followed = followed;
		this.respectSilence = respectSilence;
		this.x = followed.getX();
		this.y = followed.getY();
		this.z = followed.getZ();
		this.delay = 0;
	}

	@Override
	public void tick() {
		if ((followed.isRemoved() && respectSilence) || (followed.isSilent() && respectSilence) || Minecraft.getInstance().player == null) {
			stop();
			return;
		}

		x = followed.getX();
		y = followed.getY();
		z = followed.getZ();
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public void forceStop() {
		stop();
	}
}
