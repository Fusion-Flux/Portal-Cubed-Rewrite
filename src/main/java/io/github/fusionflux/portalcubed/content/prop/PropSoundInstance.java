package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PropSoundInstance extends FollowingSoundInstance {
	public PropSoundInstance(SoundEvent soundEvent, Prop prop) {
		super(soundEvent, SoundSource.RECORDS, prop);
	}

	@Override
	public float getVolume() {
		return super.getVolume() * (this.followed.isInWater() ? .1f : 1f);
	}
}
