package io.github.fusionflux.portalcubed.content.portal;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.ConstantPortalColor;
import io.github.fusionflux.portalcubed.content.portal.graphics.color.PortalColor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record PortalSettings(Optional<String> pair, ResourceKey<PortalType> typeId, boolean validate,
							 PortalColor color, boolean render, boolean tracer) {
	public static final Codec<PortalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("pair").forGetter(PortalSettings::pair),
			PortalType.KEY_CODEC.fieldOf("type").forGetter(PortalSettings::typeId),
			Codec.BOOL.fieldOf("validate").forGetter(PortalSettings::validate),
			PortalColor.CODEC.fieldOf("color").forGetter(PortalSettings::color),
			Codec.BOOL.fieldOf("render").forGetter(PortalSettings::render),
			Codec.BOOL.fieldOf("tracer").forGetter(PortalSettings::tracer)
	).apply(instance, PortalSettings::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalSettings> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PortalSettings::pair,
			PortalType.KEY_STREAM_CODEC, PortalSettings::typeId,
			ByteBufCodecs.BOOL, PortalSettings::validate,
			PortalColor.STREAM_CODEC, PortalSettings::color,
			ByteBufCodecs.BOOL, PortalSettings::render,
			ByteBufCodecs.BOOL, PortalSettings::tracer,
			PortalSettings::new
	);

	public static final PortalSettings DEFAULT_PRIMARY = makeDefault(Polarity.PRIMARY);
	public static final PortalSettings DEFAULT_SECONDARY = makeDefault(Polarity.SECONDARY);

	public PortalSettings(ResourceKey<PortalType> typeId, boolean validate, PortalColor color, boolean render, boolean tracer) {
		this(Optional.empty(), typeId, validate, color, render, tracer);
	}

	public String pairFor(Player user) {
		return this.pair.orElse(user.getGameProfile().getName());
	}

	private static PortalSettings makeDefault(Polarity polarity) {
		PortalColor color = new ConstantPortalColor(polarity.defaultColor);
		return new PortalSettings(Optional.empty(), PortalType.ROUND, true, color, true, true);
	}
}
