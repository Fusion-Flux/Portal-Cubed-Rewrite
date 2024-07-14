package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBrightParticle;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleDarkParticle;
import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import io.github.fusionflux.portalcubed.framework.particle.EnergySparkParticle;
import net.minecraft.core.particles.SimpleParticleType;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedParticles {
	public static final SimpleParticleType BULLET_HOLE = REGISTRAR.particles.simple("bullet_hole", () -> () -> DecalParticle.BulletHoleProvider::new);

	public static final SimpleParticleType SCORCH = REGISTRAR.particles.simple("scorch", () -> () -> DecalParticle.ScorchProvider::new);

	public static final SimpleParticleType ENERGY_SPARK = REGISTRAR.particles.simple("energy_spark", () -> () -> EnergySparkParticle.Provider::new);

	public static final SimpleParticleType FIZZLE_BRIGHT = REGISTRAR.particles.simple("fizzle_bright", () -> () -> FizzleBrightParticle.Provider::new);
	public static final SimpleParticleType FIZZLE_DARK = REGISTRAR.particles.simple("fizzle_dark", () -> () -> FizzleDarkParticle.Provider::new);
	public static final SimpleParticleType FIZZLE_FLASH = REGISTRAR.particles.simple("fizzle_flash", () -> () -> EnergySparkParticle.Provider::new);

	public static void init() {
	}
}
