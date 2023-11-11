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

	protected AbstractPathedParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
		sx = x;
		sy = y;
		sz = z;
		float angle = world.random.nextFloat() * Mth.TWO_PI;
		xd = Mth.sin(angle) * 0.0625;
		zd = Mth.cos(angle) * 0.0625;

		double xmin = -0.125f;
		double xmax = 0.125f;
		double ymin = getYOffsetForDeltaPercentage(xmin);
		double ymax = getYOffsetForDeltaPercentage(xmax);

		roll = (float) Mth.atan2(ymin - ymax, xmin - xmax) + (Mth.PI / 2);
		oRoll = roll;
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

		y = sy + getYOffsetForDeltaPercentage(age / (double)getLifetime());
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		float camRot = camera.getXRot();
		float dir = (float)(xd*Mth.cos(camRot)+zd*Mth.sin(camRot));
		if (dir > 0)
			dir = 1;
		if (dir < 0)
			dir = -1;

		float ageDelta = age + tickDelta;

		double xmin = ageDelta / (double)getLifetime() - 0.125f;
		double xmax = ageDelta / (double)getLifetime() + 0.125f;
		double ymin = getYOffsetForDeltaPercentage(xmin);
		double ymax = getYOffsetForDeltaPercentage(xmax);

		roll = (float) (Mth.atan2(ymin - ymax, xmin - xmax) * dir + (Mth.PI / 2));
		oRoll = roll;
		super.render(vertexConsumer, camera, tickDelta);
	}

	@Override
	public @NotNull ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}
}
