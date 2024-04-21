package io.github.fusionflux.portalcubed.content.portal;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;

public record PortalPair(Portal primary, Optional<Portal> maybeSecondary) {

	@Nullable
	public Portal secondary() {
		return this.maybeSecondary.orElse(null);
	}

	@Nullable
	public Portal get(PortalType type) {
		return type == PortalType.PRIMARY ? this.primary : this.secondary();
	}
}
