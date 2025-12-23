package io.github.fusionflux.portalcubed.content.portal;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.util.DualIterator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A PortalPair is a pair of linked portal instances.
 */
public record PortalPair(Optional<Portal> primary, Optional<Portal> secondary) implements Iterable<Portal> {
	public static final Codec<PortalPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Portal.CODEC.optionalFieldOf("primary").forGetter(PortalPair::primary),
			Portal.CODEC.optionalFieldOf("secondary").forGetter(PortalPair::secondary)
	).apply(instance, PortalPair::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalPair> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(Portal.STREAM_CODEC), PortalPair::primary,
			ByteBufCodecs.optional(Portal.STREAM_CODEC), PortalPair::secondary,
			PortalPair::new
	);

	public static final PortalPair EMPTY = new PortalPair(Optional.empty(), Optional.empty());

	public boolean isLinked() {
		return this.primary.isPresent() && this.secondary.isPresent();
	}

	public boolean isEmpty() {
		return this.primary.isEmpty() && this.secondary.isEmpty();
	}

	public int size() {
		if (this.primary.isPresent()) {
			return this.secondary.isPresent() ? 2 : 1;
		} else {
			return this.secondary.isPresent() ? 1 : 0;
		}
	}

	public Optional<Portal> get(Polarity polarity) {
		return polarity == Polarity.PRIMARY ? this.primary : this.secondary;
	}

	@Nullable
	public Portal getNullable(Polarity polarity) {
		return this.get(polarity).orElse(null);
	}

	public Portal getOrThrow(Polarity polarity) {
		return this.get(polarity).orElseThrow();
	}

	public boolean has(Polarity polarity) {
		return this.get(polarity).isPresent();
	}

	@Nullable
	public Portal other(Portal portal) {
		if (this.primary.isPresent() && this.primary.get() == portal) {
			return this.secondary.orElse(null);
		} else if (this.secondary.isPresent() && this.secondary.get() == portal) {
			return this.primary.orElse(null);
		} else {
			throw new IllegalArgumentException("Portal not in pair");
		}
	}

	public Polarity polarityOf(Portal portal) {
		if (this.primary.isPresent() && this.primary.get() == portal) {
			return Polarity.PRIMARY;
		} else if (this.secondary.isPresent() && this.secondary.get() == portal) {
			return Polarity.SECONDARY;
		} else {
			throw new IllegalArgumentException("Portal not in pair");
		}
	}

	public PortalPair with(Polarity polarity, @Nullable Portal portal) {
		return polarity == Polarity.PRIMARY
				? new PortalPair(Optional.ofNullable(portal), this.secondary)
				: new PortalPair(this.primary, Optional.ofNullable(portal));
	}

	public PortalPair with(Polarity polarity, @Nullable PortalData portal) {
		return this.with(polarity, portal == null ? null : new Portal(portal));
	}

	public PortalPair without(Polarity polarity) {
		return this.with(polarity, (Portal) null);
	}

	@Override
	public Iterator<Portal> iterator() {
		if (this.primary.isPresent() && this.secondary.isPresent()) {
			return new DualIterator<>(this.primary.get(), this.secondary.get());
		} else if (this.primary.isPresent()) { // no secondary
			return Iterators.singletonIterator(this.primary.get());
		} else if (this.secondary.isPresent()) { // no primary
			return Iterators.singletonIterator(this.secondary.get());
		} else { // neither
			return Collections.emptyIterator();
		}
	}
}
