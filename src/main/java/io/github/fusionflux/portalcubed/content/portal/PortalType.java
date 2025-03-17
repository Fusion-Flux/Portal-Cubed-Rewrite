package io.github.fusionflux.portalcubed.content.portal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.framework.util.EasingFunction;
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
import net.minecraft.world.level.Level;

public record PortalType(
		Component name,
		int defaultPrimaryColor,
		int defaultSecondaryColor,
		Textures textures,
		Optional<ResourceLocation> stencil,
		PlaceAnimation placeAnimation
) {
	public static final Codec<PortalType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(PortalType::name),
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default_primary_color").forGetter(PortalType::defaultPrimaryColor),
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default_secondary_color").forGetter(PortalType::defaultSecondaryColor),
			Textures.CODEC.fieldOf("textures").forGetter(PortalType::textures),
			ResourceLocation.CODEC.optionalFieldOf("stencil").forGetter(PortalType::stencil),
			PlaceAnimation.CODEC.optionalFieldOf("place_animation", PlaceAnimation.DEFAULT).forGetter(PortalType::placeAnimation)
	).apply(instance, PortalType::new));
	public static final Codec<ResourceKey<PortalType>> KEY_CODEC = ResourceKey.codec(PortalCubedRegistries.PORTAL_TYPE);
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
		public static final Codec<Textures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Layer.LIST_CODEC.fieldOf("open").forGetter(Textures::open),
				Layer.LIST_CODEC.fieldOf("closed").forGetter(Textures::closed),
				Layer.LIST_CODEC.optionalFieldOf("tracer", Collections.emptyList()).forGetter(Textures::tracer)
		).apply(instance, Textures::new));

		public record Layer(ResourceLocation texture, boolean tint, float offset) {
			public static final Codec<Layer> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("texture").forGetter(Layer::texture),
					Codec.BOOL.fieldOf("tint").forGetter(Layer::tint),
					Codec.FLOAT.optionalFieldOf("offset", 0f).forGetter(Layer::offset)
			).apply(instance, Layer::new));
			public static final Codec<Layer> CODEC = Codec.withAlternative(DIRECT_CODEC, ResourceLocation.CODEC.xmap(Layer::new, Layer::texture));
			public static final Codec<List<Layer>> LIST_CODEC = ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(CODEC));

			public Layer(ResourceLocation texture) {
				this(texture, true, 0);
			}
		}
	}

	public record PlaceAnimation(
			PortalPlaceAnimationType type,
			EasingFunction easing,
			int duration
	) {
		public static final int DEFAULT_DURATION = 5;
		public static final PlaceAnimation DEFAULT = new PlaceAnimation(PortalPlaceAnimationType.EXPAND_ALL_CENTER);

		public static final Codec<PlaceAnimation> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PortalPlaceAnimationType.CODEC.fieldOf("type").forGetter(PlaceAnimation::type),
				EasingFunction.CODEC.fieldOf("easing").forGetter(PlaceAnimation::easing),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", DEFAULT_DURATION).forGetter(PlaceAnimation::duration)
		).apply(instance, PlaceAnimation::new));
		public static final Codec<PlaceAnimation> CODEC = Codec.withAlternative(DIRECT_CODEC, PortalPlaceAnimationType.CODEC.xmap(PlaceAnimation::new, PlaceAnimation::type));

		public PlaceAnimation(PortalPlaceAnimationType type) {
			this(type, EasingFunction.LINEAR, DEFAULT_DURATION);
		}

		public float getProgress(Level level, PortalInstance portal, float tickDelta) {
			int ageInTicks = (int) (level.getGameTime() - portal.data.creationTick());
			double progress = Math.min((ageInTicks + tickDelta) / this.duration, 1);
			return (float) this.easing.apply(progress);
		}
	}
}
