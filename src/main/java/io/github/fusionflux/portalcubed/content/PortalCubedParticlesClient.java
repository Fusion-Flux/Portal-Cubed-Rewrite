package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class PortalCubedParticlesClient {
	public static void init() {
		ParticleFactoryRegistry.getInstance().register(
				PortalCubedParticles.DECAL,
				DecalParticle.Provider::new
		);
	}
}
