package io.github.fusionflux.portalcubed.framework.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

public class EnergySparkParticle extends TextureSheetParticle {
	protected final SpriteSet sprites;

	protected double sx, sy, sz;
	protected float rolld;

	protected float deltaOffset;

	protected EnergySparkParticle(ClientLevel world, double x, double y, double z, SpriteSet sprites) {
		super(world, x, y, z);
		this.sprites = sprites;
		setSpriteFromAge(this.sprites);
		this.sx = x;
		this.sy = y;
		this.sz = z;
		float angle = world.random.nextFloat() * Mth.TWO_PI;
		this.xd = Mth.sin(angle) * 0.0625;
		this.zd = Mth.cos(angle) * 0.0625;
		this.rolld = Math.signum((world.random.nextFloat() * 2 - 1)) * Mth.PI * (1 / 32f);
		this.roll = angle;
		this.oRoll = angle;

		this.deltaOffset = world.random.nextFloat() * 0.01f - 0.025f;
	}

	/**
	 * Gets the particles offset for a given percentage.
	 * This should be a pure function with no side effects.
	 *
	 * @param percentage the percentage. 0 is start, 1 is end
	 */
	public double getYOffsetForDeltaPercentage(double percentage) {
		return -Math.pow((2 * percentage - 0.5), 2) + 0.25;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
		if (getLifetime() == 0)
			return;

		this.y = sy + this.getYOffsetForDeltaPercentage(this.age / (double) this.getLifetime() + this.deltaOffset);

		this.oRoll = this.roll;

		this.roll += this.rolld;
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	public record Provider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@NotNull
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			return new EnergySparkParticle(world, x, y, z, spriteProvider);
		}
	}
}
