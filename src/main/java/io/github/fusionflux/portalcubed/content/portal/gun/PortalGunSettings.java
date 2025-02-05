package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshair;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PortalGunSettings(
		PortalSettings primary,
		Optional<PortalSettings> secondary,
		Polarity active,
		Optional<String> pair,
		PortalGunCrosshair crosshair
) {
	public static final Codec<PortalGunSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalSettings.CODEC.fieldOf("primary").forGetter(PortalGunSettings::primary),
			PortalSettings.CODEC.optionalFieldOf("secondary").forGetter(PortalGunSettings::secondary),
			Polarity.CODEC.fieldOf("active").forGetter(PortalGunSettings::active),
			Codec.STRING.optionalFieldOf("pair").forGetter(PortalGunSettings::pair),
			PortalGunCrosshair.CODEC.fieldOf("crosshair").forGetter(PortalGunSettings::crosshair)
	).apply(instance, PortalGunSettings::new));

	public static final StreamCodec<ByteBuf, PortalGunSettings> STREAM_CODEC = StreamCodec.composite(
			PortalSettings.STREAM_CODEC, PortalGunSettings::primary,
			ByteBufCodecs.optional(PortalSettings.STREAM_CODEC), PortalGunSettings::secondary,
			Polarity.STREAM_CODEC, PortalGunSettings::active,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PortalGunSettings::pair,
			PortalGunCrosshair.STREAM_CODEC, PortalGunSettings::crosshair,
			PortalGunSettings::new
	);

	public static final PortalGunSettings DEFAULT = new PortalGunSettings(
			PortalSettings.DEFAULT_PRIMARY,
			Optional.of(PortalSettings.DEFAULT_SECONDARY),
			Polarity.PRIMARY,
			Optional.empty(),
			PortalGunCrosshair.DEFAULT
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
		return new PortalGunSettings(primary, secondary, polarity, pair, crosshair);
	}
}
