package io.github.fusionflux.portalcubed.content.portal.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class PortalSoundPlayer implements PortalChangeListener {
	private final ClientLevel level;
	private final Map<PortalReference, AmbientSoundInstance> ambientSounds;

	public PortalSoundPlayer(ClientLevel level) {
		this.level = level;
		this.ambientSounds = new HashMap<>();
	}

	@Override
	public void portalCreated(PortalReference reference) {
		Portal portal = reference.get();
		this.tryPlay(reference.id, portal, PortalSounds.SoundSet::open);

		getSound(reference.id, portal, PortalSounds.SoundSet::ambient).ifPresent(sound -> {
			RandomSource random = RandomSource.create(this.level.random.nextLong());
			AmbientSoundInstance instance = new AmbientSoundInstance(sound, random, portal.origin());
			Minecraft.getInstance().getSoundManager().play(instance);
			this.ambientSounds.put(reference, instance);
		});
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		Vec3 oldPos = oldPortal.origin();
		Vec3 newPos = reference.get().origin();

		if (oldPos.equals(newPos))
			return;

		this.tryPlay(reference.id, oldPortal, PortalSounds.SoundSet::close);
		this.tryPlay(reference.id, reference.get(), PortalSounds.SoundSet::open);

		AmbientSoundInstance ambientSound = this.ambientSounds.get(reference);
		if (ambientSound != null) {
			ambientSound.setPos(newPos);
		}
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.tryPlay(reference.id, portal, PortalSounds.SoundSet::close);

		AmbientSoundInstance ambientSound = this.ambientSounds.remove(reference);
		if (ambientSound != null) {
			Minecraft.getInstance().getSoundManager().stop(ambientSound);
		}
	}

	private void tryPlay(PortalId id, Portal portal, SoundGetter<Holder<SoundEvent>> getter) {
		getSound(id, portal, getter).ifPresent(sound -> {
			Vec3 pos = portal.origin();
			this.level.playLocalSound(pos.x, pos.y, pos.z, sound.value(), SoundSource.PLAYERS, 1, 1, true);
		});
	}

	private static <T> Optional<T> getSound(PortalId id, Portal portal, SoundGetter<T> getter) {
		return getter.get(portal.type().sounds().forPolarity(id.polarity()));
	}

	@FunctionalInterface
	private interface SoundGetter<T> {
		Optional<T> get(PortalSounds.SoundSet sounds);
	}

	private static final class AmbientSoundInstance extends AbstractTickableSoundInstance implements NonTeleportableSoundInstance {
		private final PortalSounds.Ambient ambient;

		private AmbientSoundInstance(PortalSounds.Ambient ambient, RandomSource random, Vec3 initialPos) {
			super(ambient.sound().value(), SoundSource.PLAYERS, random);
			this.ambient = ambient;
			this.looping = true;
			this.setPos(initialPos);
		}

		@Override
		public void tick() {
			// we need to implement TickableSoundInstance for the sound engine to notice
			// when the position updates, but we don't actually need to do anything here.
		}

		private void setPos(Vec3 pos) {
			this.x = pos.x;
			this.y = pos.y;
			this.z = pos.z;
		}

		@Override
		public WeighedSoundEvents resolve(SoundManager manager) {
			// this is called each time the sound is played, reroll the delay
			this.ambient.delay().ifPresent(provider -> {
				int delay = provider.sample(this.random);
				// delay must always be at least 1, or else the sound will be marked as instant-looping and this will stop working
				this.delay = Math.max(delay, 1);
			});

			return super.resolve(manager);
		}
	}
}
