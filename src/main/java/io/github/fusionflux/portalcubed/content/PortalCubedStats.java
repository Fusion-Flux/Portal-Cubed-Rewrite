package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class PortalCubedStats {
	public static final ResourceLocation TIMES_DISINTEGRATED = register("times_disintegrated");
	public static final ResourceLocation PORTAL_SHOTS_HIT = register("portal_shots_hit");
	public static final ResourceLocation PORTAL_SHOTS_MISSED = register("portal_shots_missed");
	public static final ResourceLocation PORTALS_TRAVELED_THROUGH = register("portals_traveled_through");
	public static final ResourceLocation PRIMARY_PORTALS_ENTERED = register("primary_portals_entered");
	public static final ResourceLocation SECONDARY_PORTALS_ENTERED = register("secondary_portals_entered");
	public static final ResourceLocation SCIENCE_COLLABORATION_POINTS = register("science_collaboration_points");
	public static final ResourceLocation OPPORTUNITY_ADVISEMENT_POINTS = register("opportunity_advisement_points");

	private static ResourceLocation register(String name) {
		return register(name, StatFormatter.DEFAULT);
	}

	private static ResourceLocation register(String name, StatFormatter formatter) {
		ResourceLocation id = PortalCubed.id(name);
		Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
		Stats.CUSTOM.get(id, formatter); // registers the formatter
		return id;
	}

	public static void init() {
	}
}
