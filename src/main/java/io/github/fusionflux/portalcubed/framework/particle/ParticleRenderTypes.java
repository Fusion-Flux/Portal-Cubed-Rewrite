package io.github.fusionflux.portalcubed.framework.particle;

import java.util.function.Supplier;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import net.minecraft.client.particle.ParticleRenderType;

public enum ParticleRenderTypes {
	TRANSLUCENT(() -> () -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT),
	MULTIPLY(() -> () -> MultiplyParticleRenderType.INSTANCE),
	LIT(() -> () -> ParticleRenderType.PARTICLE_SHEET_LIT);

	private final Supplier<Supplier<ParticleRenderType>> supplier;

	ParticleRenderTypes(Supplier<Supplier<ParticleRenderType>> supplier) {
		this.supplier = supplier;
	}

	@ClientOnly
	public ParticleRenderType vanilla() {
		return supplier.get().get();
	}
}
