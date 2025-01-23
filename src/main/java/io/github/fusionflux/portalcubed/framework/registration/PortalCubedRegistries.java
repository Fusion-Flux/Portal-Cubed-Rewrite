package io.github.fusionflux.portalcubed.framework.registration;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class PortalCubedRegistries {
	public static final ResourceKey<Registry<Signage>> LARGE_SIGNAGE = ResourceKey.createRegistryKey(PortalCubed.id("large_signage"));
	public static final ResourceKey<Registry<Signage>> SMALL_SIGNAGE = ResourceKey.createRegistryKey(PortalCubed.id("small_signage"));

	public static void init() {
		DynamicRegistries.registerSynced(LARGE_SIGNAGE, Signage.DIRECT_CODEC);
		DynamicRegistries.registerSynced(SMALL_SIGNAGE, Signage.DIRECT_CODEC);
	}
}
