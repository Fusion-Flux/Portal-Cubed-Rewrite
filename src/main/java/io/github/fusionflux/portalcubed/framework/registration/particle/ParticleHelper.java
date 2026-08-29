package io.github.fusionflux.portalcubed.framework.registration.particle;

import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class ParticleHelper {
	private final Registrar registrar;

	public ParticleHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public <O extends ParticleOptions, T extends ParticleType<O>> ParticleBuilder<O, T> create(
			String name,
			ParticleBuilder.Provider<O, T> provider,
			Supplier<Supplier<ParticleProviderRegistry.PendingParticleProvider<O>>> clientProvider
	) {
		return new ParticleBuilderImpl<>(this.registrar, name, provider, clientProvider);
	}

	public SimpleParticleType simple(String name, Supplier<Supplier<ParticleProviderRegistry.PendingParticleProvider<SimpleParticleType>>> clientProvider) {
		return this.create(name, FabricParticleTypes::simple, clientProvider).build();
	}

	public <T extends ParticleOptions> ParticleType<T> customOptions(
			String name,
			Function<ParticleType<T>, MapCodec<T>> codecGetter,
			Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter,
			Supplier<Supplier<ParticleProviderRegistry.PendingParticleProvider<T>>> clientProvider
	) {
		return this.create(name, () -> new ParticleType<T>(true) {
			@Override
			public MapCodec<T> codec() {
				return codecGetter.apply(this);
			}

			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
				return streamCodecGetter.apply(this);
			}
		}, clientProvider).build();
	}
}
