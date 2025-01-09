package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;

public record PortalGunSettings(PortalSettings primary, Optional<PortalSettings> secondary, Polarity active, Optional<String> pair) {
	public static final Codec<PortalGunSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalSettings.CODEC.fieldOf("primary").forGetter(PortalGunSettings::primary),
			PortalSettings.CODEC.optionalFieldOf("secondary").forGetter(PortalGunSettings::secondary),
			Polarity.CODEC.fieldOf("active").forGetter(PortalGunSettings::active),
			Codec.STRING.optionalFieldOf("pair").forGetter(PortalGunSettings::pair)
	).apply(instance, PortalGunSettings::new));

	public static final PortalGunSettings DEFAULT = new PortalGunSettings(
			PortalSettings.DEFAULT_PRIMARY,
			Optional.of(PortalSettings.DEFAULT_SECONDARY),
			Polarity.PRIMARY,
			Optional.empty()
	);

	public PortalSettings effectiveSecondary() {
		return secondary.orElse(primary);
	}

	public PortalSettings activeSettings() {
		return portalSettingsOf(active);
	}

	public PortalSettings portalSettingsOf(Polarity polarity) {
		return polarity == Polarity.PRIMARY ? primary : effectiveSecondary();
	}

	public PortalGunSettings withActive(Polarity polarity) {
		return new PortalGunSettings(primary, secondary, polarity, pair);
	}
}
