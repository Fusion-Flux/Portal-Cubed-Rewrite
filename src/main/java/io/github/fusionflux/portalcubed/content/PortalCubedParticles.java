package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import io.github.fusionflux.portalcubed.framework.particle.ParaboloidParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import org.quiltmc.loader.api.minecraft.ClientOnly;

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

	@ClientOnly
	public static void initClient() {
		ParticleFactoryRegistry.getInstance().register(
				PortalCubedParticles.DECAL,
				DecalParticle.Provider::new
		);
		ParticleFactoryRegistry.getInstance().register(
				PortalCubedParticles.TEST,
				ParaboloidParticle.Provider::new
		);
	}
}
