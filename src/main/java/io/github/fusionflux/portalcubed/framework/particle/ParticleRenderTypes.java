package io.github.fusionflux.portalcubed.framework.particle;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleRenderType;

public enum ParticleRenderTypes {
	TRANSLUCENT(() -> () -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT),
	MULTIPLY(() -> () -> MultiplyParticleRenderType.INSTANCE),
	LIT(() -> () -> ParticleRenderType.PARTICLE_SHEET_LIT);

	private final Supplier<Supplier<ParticleRenderType>> supplier;

	ParticleRenderTypes(Supplier<Supplier<ParticleRenderType>> supplier) {
		this.supplier = supplier;
	}

	@Environment(EnvType.CLIENT)
	public ParticleRenderType vanilla() {
		return supplier.get().get();
	}
}
