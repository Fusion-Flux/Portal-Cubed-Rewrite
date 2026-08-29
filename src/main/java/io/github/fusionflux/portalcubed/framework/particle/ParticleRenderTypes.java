package io.github.fusionflux.portalcubed.framework.particle;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleRenderType;

public enum ParticleRenderTypes {
	SINGLE_QUADS(() -> () -> ParticleRenderType.SINGLE_QUADS),
	ITEM_PICKUP(() -> () -> ParticleRenderType.ITEM_PICKUP),
	ELDER_GUARDIANS(() -> () -> ParticleRenderType.ELDER_GUARDIANS),
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
