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
import io.github.fusionflux.portalcubed.framework.extension.ParticleEngineExt;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryStack;

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

	public static final float ONE_PIXEL = 1/16f;
	public static final double SURFACE_OFFSET = 0.01f;
	public static final int VERTEX_COUNT = 4;
	public static final int LIFETIME = 1200;

	protected final BlockPos basePos;
	protected final Quaternionf rot;
	protected final ParticleRenderType renderType;

	protected DecalParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz, BlockPos basePos, ParticleRenderType renderType) {
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

		setPos(
				x,
				y,
				z,
				dx,
				dy,
				dz
		);

		this.basePos = basePos;
		this.rot = Direction.getNearest(dx, dy, dz).getRotation();
		this.renderType = renderType;
	}

	public void setPos(double x, double y, double z, double dx, double dy, double dz) {
		// slight variance to get rid of most z fighting
		double offset = 0.01 + (random.nextDouble() * 0.001);
		this.x = snap(x) + dx * offset;
		this.y = snap(y) + dy * offset;
		this.z = snap(z) + dz * offset;
		this.xo = x;
		this.yo = y;
		this.zo = z;
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
		Vec3 camPos = camera.getPosition();
		float x = (float) (this.x - camPos.x());
		float y = (float) (this.y - camPos.y());
		float z = (float) (this.z - camPos.z());
		int light = ((ParticleEngineExt) Minecraft.getInstance().particleEngine).getDecalParticleLightCache().get(this.x, this.y, this.z);

		float u0 = getU0();
		float u1 = getU1();
		float v0 = getV0();
		float v1 = getV1();

		VertexBufferWriter writer = VertexBufferWriter.of(vertexConsumer);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(ParticleVertex.STRIDE * VERTEX_COUNT), vertex = buffer;

			vertex = writeVertex(vertex, rot, -1, -1, u1, v1, light, x, y, z);
			vertex = writeVertex(vertex, rot, -1, 1, u1, v0, light, x, y, z);
			vertex = writeVertex(vertex, rot, 1, 1, u0, v0, light, x, y, z);
			writeVertex(vertex, rot, 1, -1, u0, v1, light, x, y, z);

			writer.push(stack, buffer, VERTEX_COUNT, ParticleVertex.FORMAT);
		}
	}

	private static long writeVertex(long ptr, Quaternionf rot, float localX, float localZ, float u, float v, int light, float x, float y, float z) {
		float xx = rot.x * rot.x, yy = rot.y * rot.y, zz = rot.z * rot.z, ww = rot.w * rot.w;
		float xy = rot.x * rot.y, xz = rot.x * rot.z, yz = rot.y * rot.z, xw = rot.x * rot.w;
		float zw = rot.z * rot.w, yw = rot.y * rot.w, k = 1 / (xx + yy + zz + ww);

		float xr = 	(((xx - yy - zz + ww) * k) * (localX - ONE_PIXEL * 2)) + ((2 * (xz + yw) * k) * (localZ - ONE_PIXEL * 2));
		float yr = ((2 * (xy + zw) * k) * (localX - ONE_PIXEL * 2)) + ((2 * (yz - xw) * k) * (localZ - ONE_PIXEL * 2));
		float zr = ((2 * (xz - yw) * k) * (localX - ONE_PIXEL * 2)) + (((zz - xx - yy + ww) * k) * (localZ - ONE_PIXEL * 2));

		float vertX = (xr * .5f) + x;
		float vertY = (yr * .5f) + y;
		float vertZ = (zr * .5f) + z;

		ParticleVertex.put(ptr, vertX, vertY, vertZ, u, v, 0xFFFFFFFF, light);
		return ptr + ParticleVertex.STRIDE;
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return renderType;
	}

	public static BlockPos getBasePos(double x, double y, double z, double dx, double dy, double dz) {
		return new BlockPos(Mth.floor(x - dx * SURFACE_OFFSET), Mth.floor(y - dy * SURFACE_OFFSET), Mth.floor(z - dz * SURFACE_OFFSET));
	}

	public static double snap(double d) {
		return Math.floor(d * 16) / 16;
	}

	public record BulletHoleProvider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			BlockPos pos = getBasePos(x, y, z, dx, dy, dz);
			// Get texture and whether to multiply.
			BlockState state = world.getBlockState(pos);
			return BulletHoleMaterial.forState(state).map(material -> {
				DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, pos, material == BulletHoleMaterial.GLASS ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : PARTICLE_SHEET_MULTIPLY);
				particle.setSprite(spriteProvider.getSprites().get(material.ordinal()));
				particle.setLifetime(LIFETIME);
				return particle;
			}).orElse(null);
		}
	}

	public record ScorchProvider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@NotNull
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, getBasePos(x, y, z, dx, dy, dz), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
			particle.setSprite(Iterables.getLast(spriteProvider.getSprites()));
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
