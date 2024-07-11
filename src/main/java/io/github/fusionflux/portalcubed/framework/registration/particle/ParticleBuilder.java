package io.github.fusionflux.portalcubed.framework.registration.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public interface ParticleBuilder<O extends ParticleOptions, T extends ParticleType<O>> {
	/**
	 * Build this builder into a particle type.
	 */
	T build();

	@FunctionalInterface
	interface Provider<O extends ParticleOptions, T extends ParticleType<O>> {
		T provide();
	}
}
