package io.github.fusionflux.portalcubed.content.portal.gun;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalSettings;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.gun.crosshair.PortalGunCrosshair;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkin;
import io.github.fusionflux.portalcubed.content.portal.gun.skin.PortalGunSkinManager;
import io.github.fusionflux.portalcubed.framework.util.ClientTicks;
import io.github.fusionflux.portalcubed.framework.util.Or;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record PortalGunSettings(
		Or<PortalSettings, PortalSettings> portals,
		Polarity active,
		Optional<Polarity> lastShot,
		PortalGunCrosshair crosshair,
		ResourceKey<PortalGunSkin> skinId
) implements TooltipProvider {
	private static final Codec<Or<PortalSettings, PortalSettings>> portalsCodec = Or.codec(
			"primary", PortalSettings.CODEC, "secondary", PortalSettings.CODEC
	).codec();

	public static final Codec<PortalGunSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			portalsCodec.fieldOf("portals").forGetter(PortalGunSettings::portals),
			Polarity.CODEC.fieldOf("active").forGetter(PortalGunSettings::active),
			Polarity.CODEC.optionalFieldOf("last_shot").forGetter(PortalGunSettings::lastShot),
			PortalGunCrosshair.CODEC.fieldOf("crosshair").forGetter(PortalGunSettings::crosshair),
			ResourceKey.codec(PortalGunSkin.REGISTRY_KEY).fieldOf("skin").forGetter(PortalGunSettings::skinId)
	).apply(instance, PortalGunSettings::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PortalGunSettings> STREAM_CODEC = StreamCodec.composite(
			Or.streamCodec(PortalSettings.STREAM_CODEC, PortalSettings.STREAM_CODEC), PortalGunSettings::portals,
			Polarity.STREAM_CODEC, PortalGunSettings::active,
			ByteBufCodecs.optional(Polarity.STREAM_CODEC), PortalGunSettings::lastShot,
			PortalGunCrosshair.STREAM_CODEC, PortalGunSettings::crosshair,
			ResourceKey.streamCodec(PortalGunSkin.REGISTRY_KEY), PortalGunSettings::skinId,
			PortalGunSettings::new
	);

	public static final Map<Polarity, Component> POLARITY_TOOLTIPS = Util.makeEnumMap(Polarity.class, polarity -> PortalGunItem.translate(polarity.name + "_portal").withStyle(ChatFormatting.GRAY));

	public static final PortalGunSettings DEFAULT = new PortalGunSettings(
			PortalSettings.DEFAULTS, Polarity.PRIMARY, Optional.empty(),
			PortalGunCrosshair.DEFAULT, PortalGunSkin.DEFAULT
	);

	public static PortalGunSettings getOrDefault(ItemStack stack) {
		return stack.getOrDefault(PortalCubedDataComponents.PORTAL_GUN_SETTINGS, DEFAULT);
	}

	public Optional<PortalSettings> primary() {
		return this.portals.maybeLeft();
	}

	public Optional<PortalSettings> secondary() {
		return this.portals.maybeRight();
	}

	/**
	 * @return the primary portal settings if present, otherwise the secondary settings
	 */
	public PortalSettings primaryOrSecondary() {
		return switch (this.portals) {
			case Or.Left(PortalSettings primary) -> primary;
			case Or.Right(PortalSettings secondary) -> secondary;
			case Or.Both(PortalSettings primary, PortalSettings ignored) -> primary;
		};
	}

	/**
	 * @return the secondary portal settings if present, otherwise the primary settings
	 */
	public PortalSettings secondaryOrPrimary() {
		return switch (this.portals) {
			case Or.Left(PortalSettings primary) -> primary;
			case Or.Right(PortalSettings secondary) -> secondary;
			case Or.Both(PortalSettings ignored, PortalSettings secondary) -> secondary;
		};
	}

	/**
	 * Get the settings of the given polarity if present. Otherwise, gets the settings of the opposite polarity.
	 */
	public PortalSettings portalSettingsPreferring(Polarity polarity) {
		return switch (polarity) {
			case PRIMARY -> this.primaryOrSecondary();
			case SECONDARY -> this.secondaryOrPrimary();
		};
	}

	public Optional<PortalSettings> portalSettingsOf(Polarity polarity) {
		return switch (polarity) {
			case PRIMARY -> this.primary();
			case SECONDARY -> this.secondary();
		};
	}

	/**
	 * If this settings object only has one portal, returns its polarity. Otherwise, returns empty.
	 */
	public Optional<Polarity> polarityOfSinglePortal() {
		return switch (this.portals) {
			case Or.Left(PortalSettings ignored) -> Optional.of(Polarity.PRIMARY);
			case Or.Right(PortalSettings ignored) -> Optional.of(Polarity.SECONDARY);
			case Or.Both<PortalSettings, PortalSettings> ignored -> Optional.empty();
		};
	}

	/**
	 * Create a copy of these settings, but update the polarity of the last shot portal.
	 */
	public PortalGunSettings shoot(Polarity polarity) {
		return new PortalGunSettings(this.portals, polarity, Optional.of(polarity), this.crosshair, this.skinId);
	}

	@Environment(EnvType.CLIENT)
	@Nullable
	public PortalGunSkin skin() {
		return PortalGunSkinManager.INSTANCE.get(this.skinId);
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> output, TooltipFlag flag) {
		HolderLookup.Provider registries = context.registries();
		if (registries == null)
			return;

		boolean first = true;
		for (Polarity polarity : Polarity.values()) {
			Optional<PortalSettings> maybeSettings = this.portalSettingsOf(polarity);
			if (maybeSettings.isEmpty())
				continue;

			if (first) {
				output.accept(CommonComponents.EMPTY);
				first = false;
			}

			PortalSettings settings = maybeSettings.get();
			MutableComponent line = CommonComponents.space();

			int color = settings.color().getOpaque(ClientTicks.tryGet());
			line.append(getName(settings, registries).withColor(color));

			if (flag.isAdvanced() && settings.pair().isPresent()) {
				String pair = settings.pair().get();
				line.append(CommonComponents.space()).append(
						Component.translatable("misc.portalcubed.parentheses", pair).withStyle(ChatFormatting.DARK_GRAY)
				);
			}

			output.accept(POLARITY_TOOLTIPS.get(polarity));
			output.accept(line);
		}
	}

	private static MutableComponent getName(PortalSettings settings, HolderLookup.Provider registries) {
		Optional<Holder.Reference<PortalType>> holder = settings.resolveType(registries);
		if (holder.isPresent()) {
			return holder.get().value().name().copy();
		}

		ResourceLocation id = settings.typeId().location();
		return Component.translatableEscape("portal_type.portalcubed.invalid", id);
	}
}
