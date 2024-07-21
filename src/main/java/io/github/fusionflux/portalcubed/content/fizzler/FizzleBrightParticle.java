package io.github.fusionflux.portalcubed.content.fizzler;

import io.github.fusionflux.portalcubed.framework.particle.FadingParticle;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

public class FizzleBrightParticle extends FadingParticle {
	public static final int LIFETIME = 20;
	public static final double SPEED = 0.05;
	public static final Vec3 FADE_DIRECTION = new Vec3(0, 1, 0);
	public static final double FADE_DIRECTION_SPEED = 0.15;
	public static final float FADE_START_LIFE = 3f/4f;

	public static final float SIZE = 0.2f;

	private Vec3 direction;

	protected FizzleBrightParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
		this.direction = new Vec3(
				Math.random() * 2d - 1d,
				Math.random() * 2d - 1d,
				Math.random() * 2d - 1d
		).normalize();
		this.updateVelocity();
		this.roll = (float) (Math.random() * Mth.TWO_PI);
		this.oRoll = this.roll;
		this.quadSize = SIZE;
		this.fadeStartLife = FADE_START_LIFE;
		this.hasPhysics = false;
		this.friction = 1f;
	}

	private void updateVelocity() {
		this.xd = this.direction.x * SPEED;
		this.yd = this.direction.y * SPEED;
		this.zd = this.direction.z * SPEED;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.age >= FADE_START_LIFE * this.lifetime) {
			this.direction = this.direction.lerp(FADE_DIRECTION, FADE_DIRECTION_SPEED);
			this.updateVelocity();
		}
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getLightColor(float tint) {
		return LightTexture.FULL_BRIGHT;
	}

	public record Provider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@NotNull
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			FizzleBrightParticle particle = new FizzleBrightParticle(world, x, y, z);
			particle.pickSprite(this.spriteProvider);
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
