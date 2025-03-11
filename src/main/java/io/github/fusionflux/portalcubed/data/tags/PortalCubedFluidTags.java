package io.github.fusionflux.portalcubed.data.tags;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class PortalCubedFluidTags {
	public static final TagKey<Fluid> DOES_NOT_CLEAN_PROPS = create("does_not_clean_props");
	public static final TagKey<Fluid> HAZARDOUS_WATER = create("hazardous_water");

	private static TagKey<Fluid> create(String name) {
		return TagKey.create(Registries.FLUID, PortalCubed.id(name));
	}
}
