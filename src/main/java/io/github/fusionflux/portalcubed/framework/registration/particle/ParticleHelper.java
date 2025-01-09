package io.github.fusionflux.portalcubed.framework.registration.particle;

import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticleHelper {
	private final Registrar registrar;

	public ParticleHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public <O extends ParticleOptions, T extends ParticleType<O>> ParticleBuilder<O, T> create(
			String name,
			ParticleBuilder.Provider<O, T> provider,
			Supplier<Supplier<ParticleFactoryRegistry.PendingParticleFactory<O>>> clientFactory
	) {
		return new ParticleBuilderImpl<>(registrar, name, provider, clientFactory);
	}

	public SimpleParticleType simple(String name, Supplier<Supplier<ParticleFactoryRegistry.PendingParticleFactory<SimpleParticleType>>> clientFactory) {
		return this.create(name, FabricParticleTypes::simple, clientFactory).build();
	}
}
