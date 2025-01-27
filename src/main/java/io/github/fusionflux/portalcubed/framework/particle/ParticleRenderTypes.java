package io.github.fusionflux.portalcubed.framework.particle;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleRenderType;

public enum ParticleRenderTypes {
	OPAQUE(() -> () -> ParticleRenderType.PARTICLE_SHEET_OPAQUE),
	TRANSLUCENT(() -> () -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT),
	MULTIPLY(() -> () -> PortalCubedParticleRenderTypes.MULTIPLY),
	CUSTOM(() -> () -> ParticleRenderType.CUSTOM),
	NO_RENDER(() -> () -> ParticleRenderType.NO_RENDER);

	private final Supplier<Supplier<ParticleRenderType>> supplier;

	ParticleRenderTypes(Supplier<Supplier<ParticleRenderType>> supplier) {
		this.supplier = supplier;
	}

	@Environment(EnvType.CLIENT)
	public ParticleRenderType vanilla() {
		return supplier.get().get();
	}
}
