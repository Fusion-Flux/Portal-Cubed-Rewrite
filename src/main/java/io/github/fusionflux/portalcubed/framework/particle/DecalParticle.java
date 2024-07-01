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
import net.minecraft.Optionull;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryStack;

public class DecalParticle extends TextureSheetParticle {
	public static final ParticleRenderType MULTIPLY_RENDER_TYPE = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.SRC_COLOR);
			RenderSystem.depthMask(false);
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
			// cleanup rendering state because mojang doesn't know how to do proper render state management. Without this, fluid overlays (lava fire and water screen overlay) break while holding a block item
			RenderSystem.defaultBlendFunc();
			RenderSystem.depthMask(true);
		}
	};

	public static final float ONE_PIXEL = 1/16f;
	public static final double SURFACE_OFFSET = 0.01f;
	public static final int VERTEX_COUNT = 4;
	public static final int LIFETIME = 1200;

	protected final BlockPos basePos;
	protected final Direction direction;
	protected final Quaternionf yRot;
	protected final Quaternionf rot;
	protected final ParticleRenderType renderType;

	@Nullable
	private BlockState lastBaseState;

	protected DecalParticle(ClientLevel world, double x, double y, double z, double dx, double dy, double dz, BlockPos basePos, boolean randomRotation, ParticleRenderType renderType) {
		super(world, 0, 0, 0);

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

		this.quadSize = .5f;

		this.basePos = basePos;
		this.direction = Direction.getNearest(dx, dy, dz);
		this.rot = this.direction.getRotation();
		this.yRot = new Quaternionf();
		if (randomRotation)
			this.yRot.rotateY((Math.round(this.random.nextFloat() * 4f) / 4f) * Mth.TWO_PI);
		this.renderType = renderType;

		this.setPos(x, y, z, dx, dy, dz);
	}

	public void setPos(double x, double y, double z, double dx, double dy, double dz) {
		// slight variance to get rid of most z fighting
		double offset = 0.01 + (random.nextDouble() * 0.001);
		Direction.Axis axis = this.direction.getAxis();
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

		BlockState currentBaseState = this.level.getBlockState(this.basePos);
		if (currentBaseState.isAir()) {
			this.remove();
			return;
		}

		if (this.lastBaseState != null && this.lastBaseState != currentBaseState) {
			VoxelShape baseShape = currentBaseState.getCollisionShape(this.level, this.basePos);
			Vec3 rayStart = new Vec3(this.x, this.y, this.z);
			Vec3 rayEnd = rayStart.subtract(Vec3.atLowerCornerOf(this.direction.getNormal()).scale(ONE_PIXEL));
			if (Optionull
					.mapOrDefault(
							baseShape.clip(rayStart, rayEnd, this.basePos),
							BlockHitResult::isInside,
							true
					)
			) this.remove();
		}
		this.lastBaseState = currentBaseState;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		Vec3 camPos = camera.getPosition();
		float x = (float) (this.x - camPos.x());
		float y = (float) (this.y - camPos.y());
		float z = (float) (this.z - camPos.z());
		int light = ((ParticleEngineExt) Minecraft.getInstance().particleEngine).getDecalParticleLightCache().get(this.x, this.y, this.z);

		float u0 = this.getU0();
		float u1 = this.getU1();
		float v0 = this.getV0();
		float v1 = this.getV1();
		float size = this.getQuadSize(tickDelta);

		VertexBufferWriter writer = VertexBufferWriter.of(vertexConsumer);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(ParticleVertex.STRIDE * VERTEX_COUNT), vertex = buffer;

			vertex = writeVertex(vertex, yRot, rot, -1, -1, u1, v1, light, x, y, z, size);
			vertex = writeVertex(vertex, yRot, rot, -1, 1, u1, v0, light, x, y, z, size);
			vertex = writeVertex(vertex, yRot, rot, 1, 1, u0, v0, light, x, y, z, size);
			writeVertex(vertex, yRot, rot, 1, -1, u0, v1, light, x, y, z, size);

			writer.push(stack, buffer, VERTEX_COUNT, ParticleVertex.FORMAT);
		}
	}

	private static long writeVertex(long ptr, Quaternionf yRot, Quaternionf rot, float localX, float localZ, float u, float v, int light, float x, float y, float z, float size) {
		// inline quaternion transformations to help the JVM optimize
		float q0xx = yRot.x * yRot.x, q0yy = yRot.y * yRot.y, q0zz = yRot.z * yRot.z, q0ww = yRot.w * yRot.w;
		float q0xz = yRot.x * yRot.z;
		float q0yw = yRot.y * yRot.w, q0k = 1 / (q0xx + q0yy + q0zz + q0ww);

		float q0xr = ((((q0xx - q0yy - q0zz + q0ww) * q0k) * (localX - ONE_PIXEL)) + ((2 * (q0xz + q0yw) * q0k) * (localZ - ONE_PIXEL))) - ONE_PIXEL;
		float q0zr = (((2 * (q0xz - q0yw) * q0k) * (localX - ONE_PIXEL)) + (((q0zz - q0xx - q0yy + q0ww) * q0k) * (localZ - ONE_PIXEL))) - ONE_PIXEL;

		float q1xx = rot.x * rot.x, q1yy = rot.y * rot.y, q1zz = rot.z * rot.z, q1ww = rot.w * rot.w;
		float q1xy = rot.x * rot.y, q1xz = rot.x * rot.z, q1yz = rot.y * rot.z, q1xw = rot.x * rot.w;
		float q1zw = rot.z * rot.w, q1yw = rot.y * rot.w, q1k = 1 / (q1xx + q1yy + q1zz + q1ww);

		float xr = (((q1xx - q1yy - q1zz + q1ww) * q1k) * (q0xr)) + ((2 * (q1xz + q1yw) * q1k) * (q0zr));
		float yr = ((2 * (q1xy + q1zw) * q1k) * (q0xr)) + ((2 * (q1yz - q1xw) * q1k) * (q0zr));
		float zr = ((2 * (q1xz - q1yw) * q1k) * (q0xr)) + (((q1zz - q1xx - q1yy + q1ww) * q1k) * (q0zr));

		float vertX = (xr * size) + x;
		float vertY = (yr * size) + y;
		float vertZ = (zr * size) + z;

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
		return Math.floor(d * 16) / 16d;
	}

	public record BulletHoleProvider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			BlockPos pos = getBasePos(x, y, z, dx, dy, dz);
			// Get texture and whether to multiply.
			BlockState state = world.getBlockState(pos);
			return BulletHoleMaterial.forState(state).map(material -> {
				DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, pos, material.randomParticleRotation, material.particleRenderType.vanilla());
				particle.setLifetime(LIFETIME);
				particle.setSprite(spriteProvider.getSprites().get(material.ordinal()));
				return particle;
			}).orElse(null);
		}
	}

	public record ScorchProvider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@NotNull
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, getBasePos(x, y, z, dx, dy, dz), true, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
			particle.setSprite(Iterables.getLast(spriteProvider.getSprites()));
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
