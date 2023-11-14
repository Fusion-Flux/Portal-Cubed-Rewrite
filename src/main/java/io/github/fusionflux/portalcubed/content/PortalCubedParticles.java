package io.github.fusionflux.portalcubed.content;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedParticles {
	public static final SimpleParticleType BULLET_DECAL = REGISTRAR.particles
			.createSimple("bullet_decal")
			.provider(FabricParticleTypes::simple)
			.build();

	public static void init() {

	}
}
