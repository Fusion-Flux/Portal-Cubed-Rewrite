package io.github.fusionflux.portalcubed.content.portal.sound;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.IntProvider;

public final class PortalSounds {
	public static final Codec<PortalSounds> CODEC = RecordCodecBuilder.create(i -> i.group(
			SoundSet.CODEC.optionalFieldOf("shared", SoundSet.EMPTY).forGetter(sounds -> sounds.shared),
			SoundSet.CODEC.optionalFieldOf("primary", SoundSet.EMPTY).forGetter(sounds -> sounds.primary),
			SoundSet.CODEC.optionalFieldOf("secondary", SoundSet.EMPTY).forGetter(sounds -> sounds.secondary)
	).apply(i, PortalSounds::new));

	public static final PortalSounds EMPTY = new PortalSounds(SoundSet.EMPTY, SoundSet.EMPTY, SoundSet.EMPTY);

	private final SoundSet shared;
	private final SoundSet primary;
	private final SoundSet secondary;

	private final SoundSet primaryWithFallback;
	private final SoundSet secondaryWithFallback;

	public PortalSounds(SoundSet shared, SoundSet primary, SoundSet secondary) {
		this.shared = shared;
		this.primary = primary;
		this.secondary = secondary;

		this.primaryWithFallback = primary.withFallback(shared);
		this.secondaryWithFallback = secondary.withFallback(shared);
	}

	public SoundSet forPolarity(Polarity polarity) {
		return switch (polarity) {
			case PRIMARY -> this.primaryWithFallback;
			case SECONDARY -> this.secondaryWithFallback;
		};
	}

	public record SoundSet(Optional<Holder<SoundEvent>> open, Optional<Holder<SoundEvent>> cantOpen,
						   Optional<Holder<SoundEvent>> close, Optional<Holder<SoundEvent>> travel,
						   Optional<Ambient> ambient) {
		public static final Codec<SoundSet> CODEC = RecordCodecBuilder.create(i -> i.group(
				SoundEvent.CODEC.optionalFieldOf("open").forGetter(SoundSet::open),
				SoundEvent.CODEC.optionalFieldOf("cant_open").forGetter(SoundSet::cantOpen),
				SoundEvent.CODEC.optionalFieldOf("close").forGetter(SoundSet::close),
				SoundEvent.CODEC.optionalFieldOf("travel").forGetter(SoundSet::travel),
				Ambient.CODEC.optionalFieldOf("ambient").forGetter(SoundSet::ambient)
		).apply(i, SoundSet::new));

		public static final SoundSet EMPTY = new SoundSet(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		public SoundSet withFallback(SoundSet fallback) {
			return new SoundSet(
					this.open.or(fallback::open), this.cantOpen.or(fallback::cantOpen),
					this.close.or(fallback::close), this.travel.or(fallback::travel),
					this.ambient.or(fallback::ambient)
			);
		}
	}

	public record Ambient(Holder<SoundEvent> sound, Optional<IntProvider> delay) {
		public static final Codec<Ambient> INLINE_CODEC = SoundEvent.CODEC.xmap(Ambient::new, Ambient::sound);

		public static final Codec<Ambient> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
				SoundEvent.CODEC.fieldOf("sound").forGetter(Ambient::sound),
				IntProvider.POSITIVE_CODEC.optionalFieldOf("delay").forGetter(Ambient::delay)
		).apply(i, Ambient::new));

		public static final Codec<Ambient> CODEC = Codec.withAlternative(FULL_CODEC, INLINE_CODEC);

		public Ambient(Holder<SoundEvent> sound) {
			this(sound, Optional.empty());
		}
	}
}
