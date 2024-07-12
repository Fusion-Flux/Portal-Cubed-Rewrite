package io.github.fusionflux.portalcubed.content.fizzler;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

public class FizzleBrightParticle extends TextureSheetParticle {
	public static final double SPEED = 0.05;
	public static final float START_PROGRESS = 3f/4f;

	protected FizzleBrightParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
		Vec3 velocity = new Vec3(Math.random() * 2d - 1d, Math.random() * 2d - 1d, Math.random() * 2d - 1d)
				.normalize()
				.scale(SPEED);
		this.xd = velocity.x;
		this.yd = velocity.y;
		this.zd = velocity.z;
		this.roll = (float) (Math.random() * Mth.TWO_PI);
		this.oRoll = this.roll;
		this.hasPhysics = false;
		this.friction = 1f;
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		float fade = 1 - Math.min((Math.max(0, (Math.min((this.age + tickDelta) / this.lifetime, 1)) - START_PROGRESS) / (1 - START_PROGRESS)), 1);
		this.setAlpha(fade);
		this.quadSize = fade * 0.2f;
		super.render(vertexConsumer, camera, tickDelta);
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
			particle.setLifetime(20);
			return particle;
		}
	}
}
