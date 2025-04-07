package io.github.fusionflux.portalcubed.content.portal;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
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
public record PortalPair(Optional<PortalInstance> primary, Optional<PortalInstance> secondary) implements Iterable<PortalInstance> {
	public static final Codec<PortalPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalInstance.CODEC.optionalFieldOf("primary").forGetter(PortalPair::primary),
			PortalInstance.CODEC.optionalFieldOf("secondary").forGetter(PortalPair::secondary)
	).apply(instance, PortalPair::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalPair> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(PortalInstance.STREAM_CODEC), PortalPair::primary,
			ByteBufCodecs.optional(PortalInstance.STREAM_CODEC), PortalPair::secondary,
			PortalPair::new
	);

	public static final PortalPair EMPTY = new PortalPair(Optional.empty(), Optional.empty());

	public boolean isLinked() {
		return this.primary.isPresent() && this.secondary.isPresent();
	}

	public boolean isEmpty() {
		return this.primary.isEmpty() && this.secondary.isEmpty();
	}

	public Optional<PortalInstance> get(Polarity polarity) {
		return polarity == Polarity.PRIMARY ? this.primary : this.secondary;
	}

	public PortalInstance getNullable(Polarity polarity) {
		return this.get(polarity).orElse(null);
	}

	public PortalInstance getOrThrow(Polarity polarity) {
		return this.get(polarity).orElseThrow();
	}

	@Nullable
	public PortalInstance other(PortalInstance portal) {
		if (this.primary.isPresent() && this.primary.get() == portal) {
			return this.secondary.orElse(null);
		} else if (this.secondary.isPresent() && this.secondary.get() == portal) {
			return this.primary.orElse(null);
		} else {
			throw new IllegalArgumentException("Portal not in pair");
		}
	}

	public Polarity polarityOf(PortalInstance portal) {
		if (this.primary.isPresent() && this.primary.get() == portal) {
			return Polarity.PRIMARY;
		} else if (this.secondary.isPresent() && this.secondary.get() == portal) {
			return Polarity.SECONDARY;
		} else {
			throw new IllegalArgumentException("Portal not in pair");
		}
	}

	public PortalPair with(Polarity polarity, @Nullable PortalInstance portal) {
		return polarity == Polarity.PRIMARY
				? new PortalPair(Optional.ofNullable(portal), this.secondary)
				: new PortalPair(this.primary, Optional.ofNullable(portal));
	}

	public PortalPair without(Polarity polarity) {
		return this.with(polarity, null);
	}

	@NotNull
	@Override
	public Iterator<PortalInstance> iterator() {
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

	public record Holder(String key, PortalPair pair) {
		public PortalInstance.Holder primary() {
			PortalId id = new PortalId(this.key, Polarity.PRIMARY);
			return new PortalInstance.Holder(id, this.pair.primary.orElseThrow());
		}

		public PortalInstance.Holder secondary() {
			PortalId id = new PortalId(this.key, Polarity.SECONDARY);
			return new PortalInstance.Holder(id, this.pair.secondary.orElseThrow());
		}
	}
}
