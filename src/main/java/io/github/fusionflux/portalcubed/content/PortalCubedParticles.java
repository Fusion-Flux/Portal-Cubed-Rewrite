package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import io.github.fusionflux.portalcubed.framework.particle.EnergySparkParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedParticles {
	public static final SimpleParticleType BULLET_HOLE = REGISTRAR.particles
			.createSimple("bullet_hole")
			.provider(FabricParticleTypes::simple)
			.build();

	public static final SimpleParticleType SCORCH = REGISTRAR.particles
			.createSimple("scorch")
			.provider(FabricParticleTypes::simple)
			.build();

	public static final SimpleParticleType ENERGY_SPARK = REGISTRAR.particles
			.createSimple("energy_spark")
			.provider(FabricParticleTypes::simple)
			.build();

	public static void init() {

	}

	@ClientOnly
	public static void initClient() {
		ParticleFactoryRegistry.getInstance().register(
				PortalCubedParticles.BULLET_HOLE,
				DecalParticle.BulletHoleProvider::new
		);
		ParticleFactoryRegistry.getInstance().register(
				PortalCubedParticles.SCORCH,
				DecalParticle.ScorchProvider::new
		);
		ParticleFactoryRegistry.getInstance().register(
				PortalCubedParticles.ENERGY_SPARK,
				EnergySparkParticle.Provider::new
		);
	}
}
