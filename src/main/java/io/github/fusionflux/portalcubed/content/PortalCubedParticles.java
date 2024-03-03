package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.framework.particle.ParaboloidParticle;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedParticles {
	public static final SimpleParticleType DECAL = REGISTRAR.particles
			.createSimple("decal")
			.provider(FabricParticleTypes::simple)
			.build();

	public static final SimpleParticleType TEST = REGISTRAR.particles
			.createSimple("test")
			.provider(FabricParticleTypes::simple)
			.build();

	public static void init() {

	}
}
