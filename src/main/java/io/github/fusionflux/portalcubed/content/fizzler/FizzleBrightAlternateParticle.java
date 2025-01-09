package io.github.fusionflux.portalcubed.content.fizzler;

import org.jetbrains.annotations.NotNull;

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

public class FizzleBrightAlternateParticle extends FadingParticle {
	public static final int LIFETIME = 10;
	public static final double SPEED = 0.12;
	public static final double HORIZONTAL_SPEED = SPEED * 0.02;
	public static final float ROLL_SPEED = 8f * Mth.DEG_TO_RAD;

	public static final float SIZE = 0.2f;

	private final Vec3 direction;

	protected FizzleBrightAlternateParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
		this.direction = new Vec3(
				Math.random() * 2d - 1d,
				Math.random() * 2d,
				Math.random() * 2d - 1d
		).normalize();
		this.updateVelocity();
		this.roll = (float) (Math.random() * Mth.TWO_PI);
		this.oRoll = this.roll;
		this.quadSize = SIZE;
		this.hasPhysics = false;
		this.friction = 1f;
	}

	private void updateVelocity() {
		this.xd = this.direction.x * HORIZONTAL_SPEED;
		this.yd = this.direction.y * SPEED;
		this.zd = this.direction.z * HORIZONTAL_SPEED;
	}

	@Override
	public void tick() {
		super.tick();
		this.oRoll = this.roll;
		this.roll += ROLL_SPEED;
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
			FizzleBrightAlternateParticle particle = new FizzleBrightAlternateParticle(world, x, y, z);
			particle.pickSprite(this.spriteProvider);
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
