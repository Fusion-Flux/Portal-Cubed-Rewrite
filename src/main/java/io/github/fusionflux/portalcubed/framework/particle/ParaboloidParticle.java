package io.github.fusionflux.portalcubed.framework.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ParaboloidParticle extends AbstractPathedParticle {
	protected ParaboloidParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);

		setLifetime(20);
	}

	@Override
	public double getYOffsetForDeltaPercentage(double percentage) {
		return -Math.pow((2*percentage-0.5), 2)+0.25;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		final FabricSpriteProvider PROVIDER;

		public Provider(FabricSpriteProvider provider) {
			PROVIDER = provider;
		}

		public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel world, double d, double e, double f, double g, double h, double i) {
			ParaboloidParticle particle = new ParaboloidParticle(world, d, e, f);
			particle.setSpriteFromAge(PROVIDER);
			return particle;
		}
	}
}
