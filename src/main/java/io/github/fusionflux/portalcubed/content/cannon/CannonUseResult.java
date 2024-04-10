package io.github.fusionflux.portalcubed.content.cannon;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

public enum CannonUseResult {
	PLACED,
	OBSTRUCTED,
	MISSING_MATERIALS,
	NO_PERMS,
	MISCONFIGURED;

	public boolean shouldRecoil() {
		return this == PLACED || this == MISSING_MATERIALS;
	}

	public boolean shouldWiggle() {
		return this != PLACED && this != MISCONFIGURED;
	}

	public Optional<SoundEvent> sound() {
		return switch (this) {
			case OBSTRUCTED -> Optional.of(PortalCubedSounds.CONSTRUCTION_CANNON_OBSTRUCTED);
			case MISSING_MATERIALS -> Optional.of(PortalCubedSounds.CONSTRUCTION_CANNON_MISSING_MATERIALS);
			default -> Optional.empty();
		};
	}

	public Optional<Component> feedback(RandomSource random) {
		return Optional.ofNullable(switch (this) {
			case OBSTRUCTED -> ConstructionCannonItem.translate("feedback.obstructed");
			case MISSING_MATERIALS -> {
				String key = "feedback.missing_materials";
				if (random.nextFloat() < 0.01)
					key += ".easter_egg";
				yield ConstructionCannonItem.translate(key);
			}
			case NO_PERMS -> ConstructionCannonItem.translate("feedback.no_perms");
			default -> null;
		});
	}
}
