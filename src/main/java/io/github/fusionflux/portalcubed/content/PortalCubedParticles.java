package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import io.github.fusionflux.portalcubed.framework.particle.EnergySparkParticle;
import net.minecraft.core.particles.SimpleParticleType;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedParticles {
	public static final SimpleParticleType BULLET_HOLE = REGISTRAR.particles.simple("bullet_hole", () -> () -> DecalParticle.BulletHoleProvider::new);

	public static final SimpleParticleType SCORCH = REGISTRAR.particles.simple("scorch", () -> () -> DecalParticle.ScorchProvider::new);

	public static final SimpleParticleType ENERGY_SPARK = REGISTRAR.particles.simple("energy_spark", () -> () -> EnergySparkParticle.Provider::new);

	public static final SimpleParticleType FIZZLE = REGISTRAR.particles.simple("fizzle", () -> () -> EnergySparkParticle.Provider::new);

	public static void init() {
	}
}
