package io.github.fusionflux.portalcubed.framework.registration.particle;

import java.util.function.Supplier;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ParticleBuilderImpl<O extends ParticleOptions, T extends ParticleType<O>> implements ParticleBuilder<O, T> {
	private final Registrar registrar;
	private final String name;
	private final Provider<O, T> provider;
	private final Supplier<Supplier<ParticleProviderRegistry.PendingParticleProvider<O>>> clientProviderSupplier;

	public ParticleBuilderImpl(Registrar registrar, String name, Provider<O, T> provider, Supplier<Supplier<ParticleProviderRegistry.PendingParticleProvider<O>>> clientProvider) {
		this.registrar = registrar;
		this.name = name;
		this.provider = provider;
		this.clientProviderSupplier = clientProvider;
	}

	@Override
	public T build() {
		T type = this.provider.provide();
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.buildClient(type);
		}
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, this.registrar.id(this.name), type);
	}

	@Environment(EnvType.CLIENT)
	private void buildClient(T type) {
		ParticleProviderRegistry.getInstance().register(type, this.clientProviderSupplier.get().get());
	}
}
