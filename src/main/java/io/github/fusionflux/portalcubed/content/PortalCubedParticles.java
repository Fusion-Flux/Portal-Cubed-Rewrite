package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBrightAlternateParticle;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBrightParticle;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleDarkParticle;
import io.github.fusionflux.portalcubed.framework.particle.DecalParticle;
import net.minecraft.core.particles.SimpleParticleType;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

public class PortalCubedParticles {
	public static final SimpleParticleType BULLET_HOLE = REGISTRAR.particles.simple("bullet_hole", () -> () -> DecalParticle.BulletHoleProvider::new);

	public static final SimpleParticleType SCORCH = REGISTRAR.particles.simple("scorch", () -> () -> DecalParticle.ScorchProvider::new);

	public static final SimpleParticleType FIZZLE_BRIGHT = REGISTRAR.particles.simple("fizzle_bright", () -> () -> FizzleBrightParticle.Provider::new);
	public static final SimpleParticleType FIZZLE_BRIGHT_ALTERNATE = REGISTRAR.particles.simple("fizzle_bright_alternate", () -> () -> FizzleBrightAlternateParticle.Provider::new);
	public static final SimpleParticleType FIZZLE_DARK = REGISTRAR.particles.simple("fizzle_dark", () -> () -> FizzleDarkParticle.Provider::new);

	public static void init() {
	}
}
