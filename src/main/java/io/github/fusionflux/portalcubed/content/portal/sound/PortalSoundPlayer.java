package io.github.fusionflux.portalcubed.content.portal.sound;

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

	public PortalSoundPlayer(ClientLevel level) {
		this.level = level;
	}

	@Override
	public void portalCreated(PortalReference reference) {
		Portal portal = reference.get();
		this.tryPlay(reference.id, portal, PortalSounds.SoundSet::open);

		getSound(reference.id, portal, PortalSounds.SoundSet::ambient).ifPresent(sound -> {
			RandomSource random = RandomSource.create(this.level.random.nextLong());
			AmbientSoundInstance instance = new AmbientSoundInstance(reference, sound, random);
			Minecraft.getInstance().getSoundManager().play(instance);
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
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.tryPlay(reference.id, portal, PortalSounds.SoundSet::close);
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
		private final PortalReference portal;
		private final PortalSounds.Ambient ambient;

		private AmbientSoundInstance(PortalReference portal, PortalSounds.Ambient ambient, RandomSource random) {
			super(ambient.sound().value(), SoundSource.AMBIENT, random);
			this.portal	= portal;
			this.ambient = ambient;
			this.looping = true;
			this.updatePos();
		}

		@Override
		public void tick() {
			if (this.portal.isRemoved()) {
				this.stop();
			} else {
				this.updatePos();
			}
		}

		private void updatePos() {
			Vec3 pos = this.portal.get().origin();
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
