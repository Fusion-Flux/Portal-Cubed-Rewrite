package io.github.fusionflux.portalcubed.content.portal;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public record PortalPair(Optional<Portal> primary, Optional<Portal> secondary) {
	public static final PortalPair EMPTY = new PortalPair(Optional.empty(), Optional.empty());

	@Nullable
	public Portal getPrimary() {
		return primary.orElse(null);
	}

	@Nullable
	public Portal getSecondary() {
		return secondary.orElse(null);
	}

	@Nullable
	public Portal get(PortalType type) {
		return getOptional(type).orElse(null);
	}

	public Optional<Portal> getOptional(PortalType type) {
		return type == PortalType.PRIMARY ? primary : secondary;
	}

	public boolean isEmpty() {
		return primary.isEmpty() && secondary.isEmpty();
	}

	public PortalPair withPortal(Portal portal) {
		return switch (portal.type) {
			case PRIMARY -> new PortalPair(Optional.of(portal), this.secondary);
			case SECONDARY -> new PortalPair(this.primary, Optional.of(portal));
		};
	}

	public PortalPair withoutPortal(Portal portal) {
		PortalPair modified = switch (portal.type) {
			case PRIMARY -> new PortalPair(Optional.empty(), this.secondary);
			case SECONDARY -> new PortalPair(this.primary, Optional.empty());
		};
		return modified.isEmpty() ? EMPTY : modified;
	}
}
