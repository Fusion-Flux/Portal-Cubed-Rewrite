package io.github.fusionflux.portalcubed.content;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrateEffect;
import io.github.fusionflux.portalcubed.content.fizzler.DisintegrationSoundType;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBehaviour;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class PortalCubedRegistries {
	// static
	public static final Registry<ResourceLocation> TEST_ELEMENT_SETTINGS = simple("test_element_settings");
	public static final Registry<DisintegrateEffect> DISINTEGRATE_EFFECT = simple("disintegrate_effect");
	public static final Registry<FizzleBehaviour> FIZZLE_BEHAVIOUR = simple("fizzle_behaviour");
	// dynamic
	public static final ResourceKey<Registry<Signage>> LARGE_SIGNAGE = syncedDynamic("large_signage", Signage.DIRECT_CODEC);
	public static final ResourceKey<Registry<Signage>> SMALL_SIGNAGE = syncedDynamic("small_signage", Signage.DIRECT_CODEC);
	public static final ResourceKey<Registry<DisintegrationSoundType>> DISINTEGRATION_SOUND_TYPE = syncedDynamic("disintegration_sound_type", DisintegrationSoundType.DIRECT_CODEC);

	private static <T> Registry<T> simple(String name) {
		ResourceKey<Registry<T>> key = key(name);
		return FabricRegistryBuilder.createSimple(key).buildAndRegister();
	}

	private static <T> ResourceKey<Registry<T>> syncedDynamic(String name, Codec<T> codec) {
		ResourceKey<Registry<T>> key = key(name);
		DynamicRegistries.registerSynced(key, codec);
		return key;
	}

	private static <T> ResourceKey<Registry<T>> key(String name) {
		return ResourceKey.createRegistryKey(PortalCubed.id(name));
	}

	public static void init() {
	}
}
