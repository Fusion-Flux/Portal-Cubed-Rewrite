package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record PortalType(
		Component name,
		int defaultPrimaryColor,
		int defaultSecondaryColor,
		Textures textures,
		Optional<ResourceLocation> stencil
) {
	public static final Codec<ResourceKey<PortalType>> KEY_CODEC = ResourceKey.codec(PortalCubedRegistries.PORTAL_TYPE);
	public static final Codec<PortalType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(PortalType::name),
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default_primary_color").forGetter(PortalType::defaultPrimaryColor),
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default_secondary_color").forGetter(PortalType::defaultSecondaryColor),
			Textures.CODEC.fieldOf("textures").forGetter(PortalType::textures),
			PortalCubedCodecs.TEXTURE_PATH.optionalFieldOf("stencil").forGetter(PortalType::stencil)
	).apply(instance, PortalType::new));
	public static final Codec<Holder<PortalType>> CODEC = RegistryFixedCodec.create(PortalCubedRegistries.PORTAL_TYPE);
	public static final StreamCodec<ByteBuf, ResourceKey<PortalType>> KEY_STREAM_CODEC = ResourceKey.streamCodec(PortalCubedRegistries.PORTAL_TYPE);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<PortalType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(PortalCubedRegistries.PORTAL_TYPE);

	public static final ResourceKey<PortalType> ROUND = ResourceKey.create(PortalCubedRegistries.PORTAL_TYPE, PortalCubed.id("round"));

	public int defaultColorOf(Polarity polarity) {
		return polarity == Polarity.PRIMARY ? this.defaultPrimaryColor : this.defaultSecondaryColor;
	}

	public record Textures(
			List<Layer> open,
			List<Layer> closed,
			List<Layer> tracer
	) {
		public static final Codec<List<Layer>> TEXTURE_CODEC = ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(Layer.COMPACT_CODEC));
		public static final Codec<Textures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				TEXTURE_CODEC.fieldOf("open").forGetter(Textures::open),
				TEXTURE_CODEC.fieldOf("closed").forGetter(Textures::closed),
				TEXTURE_CODEC.optionalFieldOf("tracer", Collections.emptyList()).forGetter(Textures::tracer)
		).apply(instance, Textures::new));

		public record Layer(ResourceLocation texture, boolean tint) {
			public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("texture").forGetter(Layer::texture),
					Codec.BOOL.fieldOf("tint").forGetter(Layer::tint)
			).apply(instance, Layer::new));
			public static final Codec<Layer> COMPACT_CODEC = Codec.withAlternative(ResourceLocation.CODEC.xmap(Layer::new, Layer::texture), Layer.CODEC);

			public Layer(ResourceLocation texture) {
				this(texture, true);
			}
		}
	}
}
