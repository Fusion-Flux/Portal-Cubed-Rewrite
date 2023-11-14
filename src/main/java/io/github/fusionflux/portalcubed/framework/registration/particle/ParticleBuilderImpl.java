package io.github.fusionflux.portalcubed.framework.registration.particle;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import org.jetbrains.annotations.Nullable;

public class ParticleBuilderImpl<O extends ParticleOptions, T extends ParticleType<O>> implements ParticleBuilder<O, T> {
	private final Registrar registrar;
	private final String name;

	@Nullable
	private Provider<O, T> provider;

	public ParticleBuilderImpl(Registrar registrar, String name) {
		this.registrar = registrar;
		this.name = name;
	}

	@Override
	public ParticleBuilder<O, T> provider(Provider<O, T> provider) {
		this.provider = provider;
		return this;
	}

	@Override
	public T build() {
		if (provider == null)
			throw new NullPointerException("You must call ParticleBuilder#provider before ParticleBuilder#build!");
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, registrar.id(name), provider.provide());
	}
}
