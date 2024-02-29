package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PropSoundInstance extends FollowingSoundInstance {
	PropSoundInstance(SoundEvent soundEvent, Prop prop) {
		super(soundEvent, SoundSource.RECORDS, prop);
		setLooping(true);
	}

	@Override
	public float getVolume() {
		return followed.isInWater() ? super.getVolume() * .1f : super.getVolume();
	}
}
