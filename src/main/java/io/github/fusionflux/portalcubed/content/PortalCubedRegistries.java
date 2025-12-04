package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrateEffect;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationSoundType;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.placement.validator.PortalValidator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class PortalCubedRegistries {
	// static
	public static final Registry<ResourceLocation> TEST_ELEMENT_SETTINGS = simple("test_element_settings");
	public static final Registry<DisintegrateEffect> DISINTEGRATE_EFFECT = simple("disintegrate_effect");
	public static final Registry<PortalValidator.Type<?>> PORTAL_VALIDATOR_TYPE = simple("portal_validator_type");
	// dynamic
	public static final ResourceKey<Registry<Signage>> LARGE_SIGNAGE = key("large_signage");
	public static final ResourceKey<Registry<Signage>> SMALL_SIGNAGE = key("small_signage");
	public static final ResourceKey<Registry<DisintegrationSoundType>> DISINTEGRATION_SOUND_TYPE = key("disintegration_sound_type");
	public static final ResourceKey<Registry<PortalType>> PORTAL_TYPE = key("portal_type");

	private static <T> Registry<T> simple(String name) {
		ResourceKey<Registry<T>> key = key(name);
		return FabricRegistryBuilder.createSimple(key).buildAndRegister();
	}

	private static <T> ResourceKey<Registry<T>> key(String name) {
		return ResourceKey.createRegistryKey(PortalCubed.id(name));
	}

	public static void init() {
		// these registrations must be done here to avoid classloading the registry's type too early
		DynamicRegistries.registerSynced(LARGE_SIGNAGE, Signage.DIRECT_CODEC);
		DynamicRegistries.registerSynced(SMALL_SIGNAGE, Signage.DIRECT_CODEC);
		DynamicRegistries.registerSynced(DISINTEGRATION_SOUND_TYPE, DisintegrationSoundType.DIRECT_CODEC);
		DynamicRegistries.registerSynced(PORTAL_TYPE, PortalType.DIRECT_CODEC);
	}
}
