package io.github.fusionflux.portalcubed.framework.registration.particle;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticleHelper {
	private final Registrar registrar;

	public ParticleHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public <O extends ParticleOptions, T extends ParticleType<O>> ParticleBuilder<O, T> create(String name) {
		return new ParticleBuilderImpl<>(registrar, name);
	}

	public ParticleBuilder<SimpleParticleType, SimpleParticleType> createSimple(String name) {
		return new ParticleBuilderImpl<>(registrar, name);
	}
}
