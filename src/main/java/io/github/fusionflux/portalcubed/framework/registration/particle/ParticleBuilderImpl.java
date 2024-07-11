package io.github.fusionflux.portalcubed.framework.registration.particle;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

import java.util.function.Supplier;

public class ParticleBuilderImpl<O extends ParticleOptions, T extends ParticleType<O>> implements ParticleBuilder<O, T> {
	private final Registrar registrar;
	private final String name;
	private final Provider<O, T> provider;
	private final Supplier<Supplier<ParticleFactoryRegistry.PendingParticleFactory<O>>> clientFactorySupplier;

	public ParticleBuilderImpl(Registrar registrar, String name, Provider<O, T> provider, Supplier<Supplier<ParticleFactoryRegistry.PendingParticleFactory<O>>> clientFactory) {
		this.registrar = registrar;
		this.name = name;
		this.provider = provider;
		this.clientFactorySupplier = clientFactory;
	}

	@Override
	public T build() {
		T type = provider.provide();
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			buildClient(type);
		}
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, registrar.id(name), type);
	}

	@ClientOnly
	private void buildClient(T type) {
		ParticleFactoryRegistry.getInstance().register(type, this.clientFactorySupplier.get().get());
	}
}
