package io.github.fusionflux.portalcubed.content.fizzler;

import io.github.fusionflux.portalcubed.framework.particle.FadingParticle;
import io.github.fusionflux.portalcubed.mixin.client.ParticleAccessor;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public class FizzleDarkParticle extends FadingParticle {
	public static final int LIFETIME = 40;
	public static final double HORIZONTAL_SPEED = 0.02;
	public static final float ROLL_SPEED = 4.875f * Mth.DEG_TO_RAD;
	public static final float GRAVITY = 0.1f;

	public static final float SIZE = 0.2f;
	public static final float FADE_START_LIFE = 3.5f/4f;
	public static final float COLLISION_SIZE = 0.01f;

	protected FizzleDarkParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
		this.gravity = GRAVITY;
		this.xd = (Math.random() * 2d - 1d) * HORIZONTAL_SPEED;
		this.yd = (Math.random() * 2d - 1d) * HORIZONTAL_SPEED;
		this.quadSize = SIZE;
		this.fadeStartLife = FADE_START_LIFE;
		this.fadeAlpha = false;
		this.setSize(COLLISION_SIZE, COLLISION_SIZE);
	}

	@Override
	public void tick() {
		super.tick();
		this.oRoll = this.roll;
		if (!((ParticleAccessor) this).getStoppedByCollision())
			this.roll += ROLL_SPEED;
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	protected int getLightColor(float tint) {
		return LightTexture.FULL_BRIGHT;
	}

	public record Provider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@NotNull
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			FizzleDarkParticle particle = new FizzleDarkParticle(world, x, y, z);
			particle.pickSprite(this.spriteProvider);
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
