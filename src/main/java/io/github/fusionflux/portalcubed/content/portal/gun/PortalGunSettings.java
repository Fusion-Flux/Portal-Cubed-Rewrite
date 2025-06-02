package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.color.PortalColor;
import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshair;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkinManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record PortalGunSettings(
		PortalSettings primary,
		Optional<PortalSettings> secondary,
		Polarity active,
		Optional<String> pair,
		Optional<Polarity> shot,
		PortalGunCrosshair crosshair,
		ResourceKey<PortalGunSkin> skinId
) implements TooltipProvider {
	public static final Codec<PortalGunSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalSettings.CODEC.fieldOf("primary").forGetter(PortalGunSettings::primary),
			PortalSettings.CODEC.optionalFieldOf("secondary").forGetter(PortalGunSettings::secondary),
			Polarity.CODEC.fieldOf("active").forGetter(PortalGunSettings::active),
			Codec.STRING.optionalFieldOf("pair").forGetter(PortalGunSettings::pair),
			Polarity.CODEC.optionalFieldOf("shot").forGetter(PortalGunSettings::shot),
			PortalGunCrosshair.CODEC.fieldOf("crosshair").forGetter(PortalGunSettings::crosshair),
			ResourceKey.codec(PortalGunSkin.REGISTRY_KEY).fieldOf("skin").forGetter(PortalGunSettings::skinId)
	).apply(instance, PortalGunSettings::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalGunSettings> STREAM_CODEC = StreamCodec.composite(
			PortalSettings.STREAM_CODEC, PortalGunSettings::primary,
			ByteBufCodecs.optional(PortalSettings.STREAM_CODEC), PortalGunSettings::secondary,
			Polarity.STREAM_CODEC, PortalGunSettings::active,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PortalGunSettings::pair,
			ByteBufCodecs.optional(Polarity.STREAM_CODEC), PortalGunSettings::shot,
			PortalGunCrosshair.STREAM_CODEC, PortalGunSettings::crosshair,
			ResourceKey.streamCodec(PortalGunSkin.REGISTRY_KEY), PortalGunSettings::skinId,
			PortalGunSettings::new
	);

	public static final Map<Polarity, Component> POLARITY_TOOLTIPS = Util.makeEnumMap(Polarity.class, polarity -> PortalGunItem.translate(polarity.name + "_portal").withStyle(ChatFormatting.GRAY));

	public static final PortalGunSettings DEFAULT = builder().build();

	public static PortalGunSettings.Builder builder() {
		return new PortalGunSettings.Builder();
	}

	public PortalSettings effectiveSecondary() {
		return this.secondary.orElse(this.primary);
	}

	public PortalSettings portalSettingsOf(Polarity polarity) {
		return polarity == Polarity.PRIMARY ? this.primary : this.effectiveSecondary();
	}

	public PortalGunSettings shoot(Polarity polarity) {
		return new PortalGunSettings(this.primary, this.secondary, polarity, this.pair, Optional.of(polarity), this.crosshair, this.skinId);
	}

	@Environment(EnvType.CLIENT)
	@Nullable
	public PortalGunSkin skin() {
		return PortalGunSkinManager.INSTANCE.get(this.skinId);
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
		HolderLookup.Provider provider = context.registries();
		if (provider == null)
			return;

		boolean first = true;
		for (Polarity polarity : Polarity.values()) {
			if (polarity == Polarity.SECONDARY && this.secondary.isEmpty())
				continue;

			PortalSettings settings = this.portalSettingsOf(polarity);
			Optional<Component> typeName = provider.get(this.portalSettingsOf(polarity).typeId())
					.map(type -> type.value().name());
			if (typeName.isEmpty())
				continue;

			if (first) {
				tooltipAdder.accept(CommonComponents.EMPTY);
				first = false;
			}

			tooltipAdder.accept(POLARITY_TOOLTIPS.get(polarity));

			int color = settings.color().getOpaque(PortalColor.tryGetClientTicks());
			tooltipAdder.accept(CommonComponents.space().append(typeName.get()).withColor(color));
		}
	}

	public static final class Builder {
		private PortalSettings primary = PortalSettings.DEFAULT_PRIMARY;
		private Optional<PortalSettings> secondary = Optional.of(PortalSettings.DEFAULT_SECONDARY);
		private Polarity active = Polarity.PRIMARY;
		private Optional<String> pair = Optional.empty();
		private Optional<Polarity> shot = Optional.empty();
		private PortalGunCrosshair crosshair = PortalGunCrosshair.DEFAULT;
		private ResourceKey<PortalGunSkin> skinId = PortalGunSkin.DEFAULT;

		Builder() {
		}

		public PortalGunSettings.Builder setPrimary(PortalSettings portalSettings) {
			this.primary = portalSettings;
			return this;
		}

		public PortalGunSettings.Builder setSecondary(PortalSettings portalSettings) {
			this.secondary = Optional.of(portalSettings);
			return this;
		}

		public PortalGunSettings.Builder setActive(Polarity polarity) {
			this.active = polarity;
			return this;
		}

		public PortalGunSettings.Builder setPair(String key) {
			this.pair = Optional.of(key);
			return this;
		}

		public PortalGunSettings.Builder setShot(Polarity polarity) {
			this.shot = Optional.of(polarity);
			return this;
		}

		public PortalGunSettings.Builder setCrosshair(PortalGunCrosshair crosshair) {
			this.crosshair = crosshair;
			return this;
		}

		public PortalGunSettings.Builder setSkinId(ResourceKey<PortalGunSkin> skinId) {
			this.skinId = skinId;
			return this;
		}

		public PortalGunSettings build() {
			return new PortalGunSettings(this.primary, this.secondary, this.active, this.pair, this.shot, this.crosshair, this.skinId);
		}
	}
}
