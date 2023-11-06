package io.github.fusionflux.portalcubed.content.portal.gun;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.PortalType;

import java.util.Optional;

public record PortalGunSettings(PortalSettings primary, Optional<PortalSettings> secondary, PortalType active) {
	public static final Codec<PortalGunSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalSettings.CODEC.fieldOf("primary").forGetter(PortalGunSettings::primary),
			PortalSettings.CODEC.optionalFieldOf("secondary").forGetter(PortalGunSettings::secondary),
			PortalType.CODEC.fieldOf("active").forGetter(PortalGunSettings::active)
	).apply(instance, PortalGunSettings::new));

	public static final PortalGunSettings DEFAULT = new PortalGunSettings(
			PortalSettings.DEFAULT_PRIMARY,
			Optional.of(PortalSettings.DEFAULT_SECONDARY),
			PortalType.PRIMARY
	);

	public PortalSettings effectiveSecondary() {
		return secondary.orElse(primary);
	}

	public PortalSettings activeSettings() {
		return portalSettingsOf(active);
	}

	public PortalSettings portalSettingsOf(PortalType type) {
		return type == PortalType.PRIMARY ? primary : effectiveSecondary();
	}

	public PortalGunSettings withActive(PortalType type) {
		return new PortalGunSettings(primary, secondary, type);
	}
}
