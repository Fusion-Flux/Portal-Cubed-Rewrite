package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class PortalCubedFluids {
	public static final FlowingFluid FLOWING_GOO = register("flowing_toxic_goo", new GooFluid.Flowing());
	public static final FlowingFluid GOO = register("toxic_goo", new GooFluid.Source());

	public static <T extends Fluid> T register(String name, T value) {
		return Registry.register(BuiltInRegistries.FLUID, PortalCubed.id(name), value);
	}

	public static void init() {
	}
}
