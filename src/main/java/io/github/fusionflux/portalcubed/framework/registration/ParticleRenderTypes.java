package io.github.fusionflux.portalcubed.framework.registration;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import net.minecraft.client.particle.ParticleRenderType;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.function.Supplier;

public enum ParticleRenderTypes {
	TRANSLUCENT(() -> () -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT),
	MULTIPLY(() -> () -> DecalParticle.MULTIPLY_RENDER_TYPE),
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
