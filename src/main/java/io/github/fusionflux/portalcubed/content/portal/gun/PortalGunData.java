package io.github.fusionflux.portalcubed.content.portal.gun;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalType;

import java.util.Optional;

public record PortalGunData(PortalData primary, Optional<PortalData> secondary, PortalType active) {
	public static final Codec<PortalGunData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalData.CODEC.fieldOf("primary").forGetter(PortalGunData::primary),
			PortalData.CODEC.optionalFieldOf("secondary").forGetter(PortalGunData::secondary),
			PortalType.CODEC.fieldOf("active").forGetter(PortalGunData::active)
	).apply(instance, PortalGunData::new));

	public static final PortalGunData DEFAULT = new PortalGunData(
			PortalData.DEFAULT_PRIMARY,
			Optional.of(PortalData.DEFAULT_SECONDARY),
			PortalType.PRIMARY
	);

	public PortalData effectiveSecondary() {
		return secondary.orElse(primary);
	}

	public PortalData activeData() {
		return portalDataOf(active);
	}

	public PortalData portalDataOf(PortalType type) {
		return type == PortalType.PRIMARY ? primary : effectiveSecondary();
	}

	public PortalGunData withActive(PortalType type) {
		return new PortalGunData(primary, secondary, type);
	}
}
