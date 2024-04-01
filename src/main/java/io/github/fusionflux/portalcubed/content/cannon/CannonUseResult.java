package io.github.fusionflux.portalcubed.content.cannon;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.sounds.SoundEvent;

public enum CannonUseResult {
	PLACED,
	OBSTRUCTED,
	MISSING_MATERIALS,
	NOT_CONFIGURED,
	NO_PERMS,
	INVALID;

	public boolean shouldRecoil() {
		return this == PLACED;
	}

	public boolean shouldWiggle() {
		return this != PLACED;
	}

	public Optional<SoundEvent> sound() {
		return switch (this) {
			case OBSTRUCTED -> Optional.of(PortalCubedSounds.CONSTRUCTION_CANNON_OBSTRUCTED);
			case MISSING_MATERIALS -> Optional.of(PortalCubedSounds.CONSTRUCTION_CANNON_NO_MATERIALS);
			default -> Optional.empty();
		};
	}
}
