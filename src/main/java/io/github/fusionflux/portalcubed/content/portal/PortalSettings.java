package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record PortalSettings(ResourceKey<PortalType> typeId, boolean validate, PortalColor color, boolean render) {
	public static final Codec<PortalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalType.KEY_CODEC.fieldOf("type").forGetter(PortalSettings::typeId),
			Codec.BOOL.fieldOf("validate").forGetter(PortalSettings::validate),
			PortalColor.CODEC.fieldOf("color").forGetter(PortalSettings::color),
			Codec.BOOL.fieldOf("render").forGetter(PortalSettings::render)
	).apply(instance, PortalSettings::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalSettings> STREAM_CODEC = StreamCodec.composite(
			PortalType.KEY_STREAM_CODEC, PortalSettings::typeId,
			ByteBufCodecs.BOOL, PortalSettings::validate,
			PortalColor.STREAM_CODEC, PortalSettings::color,
			ByteBufCodecs.BOOL, PortalSettings::render,
			PortalSettings::new
	);

	public static final PortalSettings DEFAULT_PRIMARY = makeDefault(Polarity.PRIMARY);
	public static final PortalSettings DEFAULT_SECONDARY = makeDefault(Polarity.SECONDARY);

	private static PortalSettings makeDefault(Polarity polarity) {
		PortalColor color = new ConstantPortalColor(polarity.defaultColor);
		return new PortalSettings(PortalType.ROUND, true, color, true);
	}
}
