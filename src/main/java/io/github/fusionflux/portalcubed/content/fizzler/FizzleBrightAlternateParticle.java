package io.github.fusionflux.portalcubed.content.fizzler;

import io.github.fusionflux.portalcubed.framework.particle.FadingParticle;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class FizzleBrightAlternateParticle extends FadingParticle {
	public static final int LIFETIME = 10;
	public static final double SPEED = 0.12;
	public static final double HORIZONTAL_SPEED = SPEED * 0.02;
	public static final float ROLL_SPEED = 8f * Mth.DEG_TO_RAD;

	public static final float SIZE = 0.2f;

	private final Vec3 direction;

	protected FizzleBrightAlternateParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
		super(level, x, y, z, sprite);
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

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	@Override
	protected int getLightCoords(float a) {
		return LightCoordsUtil.FULL_BRIGHT;
	}

	public record Provider(FabricSpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
			FizzleBrightAlternateParticle particle = new FizzleBrightAlternateParticle(level, x, y, z, this.spriteSet.get(random));
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
