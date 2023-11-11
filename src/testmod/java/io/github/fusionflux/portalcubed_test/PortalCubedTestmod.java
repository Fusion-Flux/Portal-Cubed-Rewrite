package io.github.fusionflux.portalcubed_test;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalCubedTestmod implements ModInitializer {
	public static final SimpleParticleType TEST_PARTICLE = FabricParticleTypes.simple();

	public static final String ID = "portalcubed_test";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("test_particle"), TEST_PARTICLE);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
