package io.github.fusionflux.portalcubed.content.portal.gun.crosshair;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.Polarity;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record PortalGunCrosshairType(
		Component name,
		boolean removeVanillaCrosshair,
		Optional<ResourceLocation> base,
		Indicator primary,
		Indicator secondary
) {
	public static final Codec<PortalGunCrosshairType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(PortalGunCrosshairType::name),
			Codec.BOOL.optionalFieldOf("remove_vanilla_crosshair", false).forGetter(PortalGunCrosshairType::removeVanillaCrosshair),
			ResourceLocation.CODEC.optionalFieldOf("base").forGetter(PortalGunCrosshairType::base),
			Indicator.CODEC.fieldOf("primary").forGetter(PortalGunCrosshairType::primary),
			Indicator.CODEC.fieldOf("secondary").forGetter(PortalGunCrosshairType::secondary)
	).apply(instance, PortalGunCrosshairType::new));
	public static final ResourceKey<Registry<PortalGunCrosshairType>> REGISTRY_KEY = ResourceKey.createRegistryKey(PortalCubed.id("portal_gun_crosshair_type"));

	public Indicator indicatorOf(Polarity polarity) {
		return polarity == Polarity.PRIMARY ? this.primary : this.secondary;
	}

	public record Indicator(ResourceLocation empty, ResourceLocation placed, Optional<ResourceLocation> lastPlaced) {
		public static final Codec<Indicator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("empty").forGetter(Indicator::empty),
				ResourceLocation.CODEC.fieldOf("placed").forGetter(Indicator::placed),
				ResourceLocation.CODEC.optionalFieldOf("last_placed").forGetter(Indicator::lastPlaced)
		).apply(instance, Indicator::new));
	}
}
