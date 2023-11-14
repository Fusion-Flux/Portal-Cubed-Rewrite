package io.github.fusionflux.portalcubed.framework.registration.particle;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public interface ParticleBuilder<O extends ParticleOptions, T extends ParticleType<O>> {
	ParticleBuilder<O, T> provider(Provider<O, T> provider);

	T build();

	@FunctionalInterface
	interface Provider<O extends ParticleOptions, T extends ParticleType<O>> {
		T provide();
	}
}
