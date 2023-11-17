package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
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

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DecalParticle extends TextureSheetParticle {
	public static final ParticleRenderType PARTICLE_SHEET_MULTIPLY = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.depthMask(true);
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.SRC_COLOR);
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

	final Quaternionf rotation;
	final BlockPos basePos;
	final boolean multiply;

	protected DecalParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz, BlockPos basePos, boolean multiply) {
		super(clientLevel, Math.round(x * 16) / 16f + dx * 0.01f, Math.round(y * 16) / 16f + dy * 0.01f, Math.round(z * 16) / 16f + dz * 0.01f);

		// Keep track of some things.
		this.basePos = basePos;
		this.multiply = multiply;

		// rotate the particle to be oriented in the right direction.
		float rx = (float)Math.asin(dy);
		float ry = (float)Math.atan2(dx, dz) + Mth.PI;
		float rz = Math.round(clientLevel.random.nextFloat() * 4) / 4f * Mth.TWO_PI;

		rotation = new Quaternionf().rotateY(ry).rotateX(rx).rotateZ(rz);

		// Idk if this is the best place to put this.
		setLifetime(1200);
	}

	@Override
	public void tick() {
		super.tick();
		// Is it broken? Die.
		if (level.getBlockState(basePos).isAir())
			remove();
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		Vec3 vec3 = camera.getPosition();
		float f = (float)(Mth.lerp(tickDelta, xo, x) - vec3.x());
		float g = (float)(Mth.lerp(tickDelta, yo, y) - vec3.y());
		float h = (float)(Mth.lerp(tickDelta, zo, z) - vec3.z());

		Vector3f[] vector3fs = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
		};

		for(int j = 0; j < 4; ++j) {
			Vector3f vector3f = vector3fs[j];
			vector3f.rotate(rotation);
			// I love magic numbers.
			vector3f.mul(0.5f);
			vector3f.add(f, g, h);
		}

		float k = getU0();
		float l = getU1();
		float m = getV0();
		float n = getV1();
		int o = getLightColor(tickDelta);
		vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
				.uv(l, n)
				.color(rCol, gCol, bCol, alpha)
				.uv2(o)
				.endVertex();
		vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
				.uv(l, m)
				.color(rCol, gCol, bCol, alpha)
				.uv2(o)
				.endVertex();
		vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
				.uv(k, m)
				.color(rCol, gCol, bCol, alpha)
				.uv2(o)
				.endVertex();
		vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
				.uv(k, n)
				.color(rCol, gCol, bCol, alpha)
				.uv2(o)
				.endVertex();
	}

	@Override
	public ParticleRenderType getRenderType() {
		return multiply ? PARTICLE_SHEET_MULTIPLY : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		final FabricSpriteProvider PROVIDER;

		public Provider(FabricSpriteProvider provider) {
			PROVIDER = provider;
		}

		public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			BlockPos pos = new BlockPos((int) Math.floor(x - dx * 0.02f), (int) Math.floor(y - dy* 0.02f), (int) Math.floor(z - dz* 0.02f));

			// Get texture and whether to multiply.
			BlockState state = world.getBlockState(pos);
			int texture = getTextureForState(state);
			boolean multiply = shouldMultiply(state);

			DecalParticle particle = new DecalParticle(world, x, y, z, dx, dy, dz, pos, multiply);
			particle.setSprite(PROVIDER.getSprites().get(texture));
			return particle;
		}

		private static int getTextureForState(BlockState state) {
			if (state.is(PortalCubedBlockTags.BULLET_HOLE_CONCRETE))
				return 0;
			if (state.is(PortalCubedBlockTags.BULLET_HOLE_GLASS))
				return 1;
			if (state.is(PortalCubedBlockTags.BULLET_HOLE_METAL))
				return 2;
			return 3;
		}

		private static boolean shouldMultiply(BlockState state) {
			return state.is(PortalCubedBlockTags.BULLET_HOLE_CONCRETE)
					|| state.is(PortalCubedBlockTags.BULLET_HOLE_METAL);
		}
	}
}
