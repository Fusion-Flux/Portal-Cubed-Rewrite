package io.github.fusionflux.portalcubed.content;

import static io.github.fusionflux.portalcubed.PortalCubed.REGISTRAR;

import io.github.fusionflux.portalcubed.content.fizzler.FizzleBrightAlternateParticle;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleBrightParticle;
import io.github.fusionflux.portalcubed.content.fizzler.FizzleDarkParticle;
import io.github.fusionflux.portalcubed.content.misc.DecalParticle;
import io.github.fusionflux.portalcubed.content.portal.graphics.PortalProjectileParticle;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class PortalCubedParticles {
	// TODO: concrete and metal should use a multiply layer
	public static final SimpleParticleType CONCRETE_BULLET_HOLE = bulletHole("concrete_bullet_hole", SingleQuadParticle.Layer.TRANSLUCENT, false);
	public static final SimpleParticleType GLASS_BULLET_HOLE = bulletHole("glass_bullet_hole", SingleQuadParticle.Layer.TRANSLUCENT, true);
	public static final SimpleParticleType METAL_BULLET_HOLE = bulletHole("metal_bullet_hole", SingleQuadParticle.Layer.TRANSLUCENT, false);
	public static final SimpleParticleType SCORCH = REGISTRAR.particles.simple("scorch", () -> () -> DecalParticle.ScorchProvider::new);

	public static final SimpleParticleType FIZZLE_BRIGHT = REGISTRAR.particles.simple("fizzle_bright", () -> () -> FizzleBrightParticle.Provider::new);
	public static final SimpleParticleType FIZZLE_BRIGHT_ALTERNATE = REGISTRAR.particles.simple("fizzle_bright_alternate", () -> () -> FizzleBrightAlternateParticle.Provider::new);
	public static final SimpleParticleType FIZZLE_DARK = REGISTRAR.particles.simple("fizzle_dark", () -> () -> FizzleDarkParticle.Provider::new);
	public static final SimpleParticleType LEMON_LEAVES = REGISTRAR.particles.simple("lemon_leaves", () -> () -> FallingLeavesParticle.PoplarProvider::new);

	public static final ParticleType<CustomTrailParticleOption> PORTAL_PROJECTILE = REGISTRAR.particles.customOptions(
			"portal_projectile",
			CustomTrailParticleOption::codec,
			CustomTrailParticleOption::streamCodec,
			() -> () -> PortalProjectileParticle.Provider::new
	);

	public static void init() {
	}

	private static SimpleParticleType bulletHole(String name, SingleQuadParticle.Layer layer, boolean randomRotation) {
		return REGISTRAR.particles.simple(name, () -> () -> DecalParticle.bulletHoleProvider(layer, randomRotation));
	}
}
