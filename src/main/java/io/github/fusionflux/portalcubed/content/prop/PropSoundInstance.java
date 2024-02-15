package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PropSoundInstance extends AbstractTickableSoundInstance {
	private final Prop prop;

	PropSoundInstance(SoundEvent soundEvent, Prop prop) {
		super(soundEvent, SoundSource.RECORDS, prop.level().random);
		this.prop = prop;
		this.x = prop.getX();
		this.y = prop.getY();
		this.z = prop.getZ();
		this.delay = 0;
		this.looping = true;
	}

	@Override
	public void tick() {
		var client = Minecraft.getInstance();
		if (prop.isRemoved() || client.player == null) {
			stop();
			return;
		}

		x = prop.getX();
		y = prop.getY();
		z = prop.getZ();
	}

	@Override
	public float getVolume() {
		return prop.isInWater() ? super.getVolume() * .1f : super.getVolume();
	}

	public void forceStop() {
		stop();
	}
}
