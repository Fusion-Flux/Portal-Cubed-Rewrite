package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

public record PortalSettings(ResourceKey<PortalType> typeId, boolean validate, int color, boolean render) {
	public static final Codec<PortalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalType.KEY_CODEC.fieldOf("type").forGetter(PortalSettings::typeId),
			Codec.BOOL.fieldOf("validate").forGetter(PortalSettings::validate),
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(PortalSettings::color),
			Codec.BOOL.fieldOf("render").forGetter(PortalSettings::render)
	).apply(instance, PortalSettings::new));
	public static final StreamCodec<ByteBuf, PortalSettings> STREAM_CODEC = StreamCodec.composite(
			PortalType.KEY_STREAM_CODEC, PortalSettings::typeId,
			ByteBufCodecs.BOOL, PortalSettings::validate,
			ByteBufCodecs.INT, PortalSettings::color,
			ByteBufCodecs.BOOL, PortalSettings::render,
			PortalSettings::new
	);

	public static final PortalSettings DEFAULT_PRIMARY = new PortalSettings(PortalType.ROUND, true, Polarity.PRIMARY.defaultColor, true);
	public static final PortalSettings DEFAULT_SECONDARY = new PortalSettings(PortalType.ROUND, true, Polarity.SECONDARY.defaultColor, true);
}
