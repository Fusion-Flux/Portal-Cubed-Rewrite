package io.github.fusionflux.portalcubed.framework.particle;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.content.misc.BulletHoleMaterial;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DecalParticle extends TextureSheetParticle {
	public static final ParticleRenderType PARTICLE_SHEET_MULTIPLY = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.SRC_COLOR);
			RenderSystem.depthMask(false);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
		}

		public String toString() {
			return "PORTALCUBED#PARTICLE_SHEET_MULTIPLY";
		}
	};

	public final float ONE_PIXEL = 1/16f;

	protected final Quaternionf rotationXY;
	protected final Quaternionf rotationZ;
	protected final BlockPos basePos;
	protected final boolean multiply;

	protected final int rotationValue;

	protected DecalParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz, BlockPos basePos, boolean multiply) {
		super(clientLevel, 0, 0, 0);

		if (dz > 0) {
			x += ONE_PIXEL;
		} if (dx < 0) {
			z += ONE_PIXEL;
		} else if (dy > 0) {
			x += ONE_PIXEL;
			z += ONE_PIXEL;
		} else if (dy < 0) {
			x += ONE_PIXEL;
		}


		setPos(x, y, z, dx, dy, dz);

		// Keep track of some things.
		this.basePos = basePos;
		this.multiply = multiply;

		// rotate the particle to be oriented in the right direction.
		float rx = (float)Math.asin(dy);
		float ry = (float)Math.atan2(dx, dz) + Mth.PI;
		rotationValue = Math.round(clientLevel.random.nextFloat() * 4);
		float rz = rotationValue / 4f * Mth.TWO_PI;

		rotationXY = new Quaternionf().rotateY(ry).rotateX(rx);
		rotationZ = new Quaternionf().rotateZ(rz);

		// Idk if this is the best place to put this.
		setLifetime(1200);
	}

	public void setPos(double x, double y, double z, double dx, double dy, double dz) {
		// slight variance to get rid of most z fighting
		double offset = 0.01 + (random.nextDouble() * 0.001);
		this.x = snap(x) + dx * offset;
		this.y = snap(y) + dy * offset;
		this.z = snap(z) + dz * offset;
		xo = x;
		yo = y;
		zo = z;
	}

	@Override
	public void tick() {
		super.tick();
		// Is it broken? Die.
		if (this.level.getBlockState(basePos).isAir())
			remove();
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		Vec3 vec3 = camera.getPosition();

		// We don't need to lerp it as it's always static.
		float px = (float)(x - vec3.x());
		float py = (float)(y - vec3.y());
		float pz = (float)(z - vec3.z());

		Vector3f[] vector3fs = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
		};

		for(int j = 0; j < 4; ++j) {
			Vector3f vector3f = vector3fs[j];
			vector3f.sub(ONE_PIXEL, ONE_PIXEL, 0);
			vector3f.rotate(rotationZ);
			vector3f.add(ONE_PIXEL, ONE_PIXEL, 0);
			vector3f.rotate(rotationXY);

			// I love magic numbers.
			vector3f.mul(0.5f);
			vector3f.add(px, py, pz);
		}

		float u0 = getU0();
		float u1 = getU1();
		float v0 = getV0();
		float v1 = getV1();
		int lightColor = getLightColor(tickDelta);
		vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
				.uv(u1, v1)
				.color(rCol, gCol, bCol, alpha)
				.uv2(lightColor)
				.endVertex();
		vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
				.uv(u1, v0)
				.color(rCol, gCol, bCol, alpha)
				.uv2(lightColor)
				.endVertex();
		vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
				.uv(u0, v0)
				.color(rCol, gCol, bCol, alpha)
				.uv2(lightColor)
				.endVertex();
		vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
				.uv(u0, v1)
				.color(rCol, gCol, bCol, alpha)
				.uv2(lightColor)
				.endVertex();
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return multiply ? PARTICLE_SHEET_MULTIPLY : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static BlockPos getBasePos(double x, double y, double z, double dx, double dy, double dz) {
		return new BlockPos((int) Math.floor(x - dx * 0.02f), (int) Math.floor(y - dy* 0.02f), (int) Math.floor(z - dz* 0.02f));
	}

	public static double snap(double d) {
		return Math.floor(d * 16) / 16d;
	}

	public static class BulletHoleProvider implements ParticleProvider<SimpleParticleType> {
		private final FabricSpriteProvider spriteProvider;

		public BulletHoleProvider(FabricSpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			BlockPos pos = getBasePos(x, y, z, dx, dy, dz);
			// Get texture and whether to multiply.
			BlockState state = world.getBlockState(pos);
			return BulletHoleMaterial.forState(state).map(material -> {
				DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, pos, material != BulletHoleMaterial.GLASS);
				particle.setSprite(spriteProvider.getSprites().get(material.ordinal()));
				return particle;
			}).orElse(null);
		}
	}

	public static class ScorchProvider implements ParticleProvider<SimpleParticleType> {
		private final FabricSpriteProvider spriteProvider;

		public ScorchProvider(FabricSpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, getBasePos(x, y, z, dx, dy, dz), false);
			particle.setSprite(Iterables.getLast(spriteProvider.getSprites()));
			return particle;
		}
	}
}
