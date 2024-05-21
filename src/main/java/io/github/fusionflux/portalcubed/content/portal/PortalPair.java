package io.github.fusionflux.portalcubed.content.portal;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.util.DualIterator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A PortalPair is a pair of linked portal instances.
 */
public record PortalPair(Optional<PortalInstance> primary, Optional<PortalInstance> secondary) implements Iterable<PortalInstance> {
	public static final Codec<PortalPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalInstance.CODEC.optionalFieldOf("primary").forGetter(PortalPair::primary),
			PortalInstance.CODEC.optionalFieldOf("secondary").forGetter(PortalPair::secondary)
	).apply(instance, PortalPair::new));

	public Optional<PortalInstance> get(PortalType type) {
		return type == PortalType.PRIMARY ? this.primary : this.secondary;
	}

	public PortalInstance getNullable(PortalType type) {
		return this.get(type).orElse(null);
	}

	public PortalPair with(PortalType type, @Nullable PortalInstance portal) {
		return type == PortalType.PRIMARY
				? new PortalPair(Optional.ofNullable(portal), this.secondary)
				: new PortalPair(this.primary, Optional.ofNullable(portal));
	}

	public PortalPair without(PortalType type) {
		return this.with(type, null);
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
}
