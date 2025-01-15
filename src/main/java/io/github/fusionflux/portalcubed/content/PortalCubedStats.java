package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class PortalCubedStats {
	public static final ResourceLocation TIMES_DISINTEGRATED = register("times_disintegrated", StatFormatter.DEFAULT);

	private static ResourceLocation register(String name, StatFormatter formatter) {
		ResourceLocation id = PortalCubed.id(name);
		Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
		Stats.CUSTOM.get(id, formatter); // registers the formatter
		return id;
	}

	public static void init() {
	}
}
