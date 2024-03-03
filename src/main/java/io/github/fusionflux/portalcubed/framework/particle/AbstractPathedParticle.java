package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

import net.minecraft.client.particle.ParticleRenderType;

import net.minecraft.client.particle.TextureSheetParticle;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class AbstractPathedParticle extends TextureSheetParticle {
	public double sx, sy, sz;
	public float rolld;

	public float deltaOffset;

	protected AbstractPathedParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
		sx = x;
		sy = y;
		sz = z;
		float angle = world.random.nextFloat() * Mth.TWO_PI;
		xd = Mth.sin(angle) * 0.0625;
		zd = Mth.cos(angle) * 0.0625;
		rolld = Math.signum((world.random.nextFloat() * 2 - 1)) * Mth.PI * (1/32f);
		roll = angle;
		oRoll = angle;

		deltaOffset = world.random.nextFloat() * 0.01f - 0.025f;
	}

	/**
	 * Gets the particles offset for a given percentage.
	 * This should be a pure function with no side effects.
	 * @param percentage the percentage. 0 is start, 1 is end
	 * */
	@Contract(pure = true)
    public abstract double getYOffsetForDeltaPercentage(double percentage);

	@Override
	public void tick() {
		super.tick();
		if (getLifetime() == 0)
			return;

		y = sy + getYOffsetForDeltaPercentage(age / (double)getLifetime() + deltaOffset);

		oRoll = roll;

		roll += rolld;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		super.render(vertexConsumer, camera, tickDelta);
	}

	@Override
	public @NotNull ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}
}
