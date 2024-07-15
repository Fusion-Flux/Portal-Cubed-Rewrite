package io.github.fusionflux.portalcubed.content.misc;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.Optionull;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

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
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class DecalParticle extends TextureSheetParticle {
	public static final float ONE_PIXEL = 1/16f;
	public static final double SURFACE_OFFSET = 0.01f;
	public static final int VERTEX_COUNT = 4;
	public static final int LIFETIME = 1200;

	private final BlockPos basePos;
	private final Direction direction;
	private final Matrix4f matrix;
	private final ParticleRenderType renderType;

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
		this.matrix = new Matrix4f()
				.rotate(this.direction.getRotation())
				.translate(-ONE_PIXEL, 0, -ONE_PIXEL)
				.rotateY(randomRotation ? (Math.round(this.random.nextFloat() * 4f) / 4f) * Mth.TWO_PI : 0)
				.translate(-ONE_PIXEL, 0, -ONE_PIXEL);
		this.renderType = renderType;

		this.setPos(x, y, z, dx, dy, dz);
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

		int light = this.getLightColor(tickDelta);
		float u0 = this.getU0();
		float u1 = this.getU1();
		float v0 = this.getV0();
		float v1 = this.getV1();
		float size = this.getQuadSize(tickDelta);

		VertexBufferWriter writer = VertexBufferWriter.of(vertexConsumer);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(ParticleVertex.STRIDE * VERTEX_COUNT), vertex = buffer;

			vertex = writeVertex(vertex, this.matrix, -1, -1, u1, v1, light, x, y, z, size);
			vertex = writeVertex(vertex, this.matrix, -1, 1, u1, v0, light, x, y, z, size);
			vertex = writeVertex(vertex, this.matrix, 1, 1, u0, v0, light, x, y, z, size);
			vertex = writeVertex(vertex, this.matrix, 1, -1, u0, v1, light, x, y, z, size);

			writer.push(stack, buffer, VERTEX_COUNT, ParticleVertex.FORMAT);
		}
	}

	private static long writeVertex(long ptr, Matrix4f matrix, float localX, float localZ, float u, float v, int light, float x, float y, float z, float size) {
		float vertX = (MatrixHelper.transformPositionX(matrix, localX, 0, localZ) * size) + x;
		float vertY = (MatrixHelper.transformPositionY(matrix, localX, 0, localZ) * size) + y;
		float vertZ = (MatrixHelper.transformPositionZ(matrix, localX, 0, localZ) * size) + z;

		ParticleVertex.put(ptr, vertX, vertY, vertZ, u, v, 0xFFFFFFFF, light);
		return ptr + ParticleVertex.STRIDE;
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return this.renderType;
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
				particle.setSprite(this.spriteProvider.getSprites().get(material.ordinal()));
				return particle;
			}).orElse(null);
		}
	}

	public record ScorchProvider(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@NotNull
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, getBasePos(x, y, z, dx, dy, dz), true, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
			particle.pickSprite(this.spriteProvider);
			particle.setLifetime(LIFETIME);
			return particle;
		}
	}
}
