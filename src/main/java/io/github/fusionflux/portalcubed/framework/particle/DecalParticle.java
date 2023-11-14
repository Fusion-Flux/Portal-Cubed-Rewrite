package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DecalParticle extends TextureSheetParticle {
	final Quaternionf rotation;

	protected DecalParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz) {
		super(clientLevel, x, y, z);
		float rx = (float)Math.asin(dy);
		float ry = (float)Math.atan2(dx, dz) + Mth.PI;
		float rz = clientLevel.random.nextFloat() * Mth.TWO_PI;
		rotation = new Quaternionf().rotateY(ry).rotateX(rx).rotateZ(rz);
		setLifetime(1200);
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
		float i = getQuadSize(tickDelta);

		for(int j = 0; j < 4; ++j) {
			Vector3f vector3f = vector3fs[j];
			vector3f.rotate(rotation);
			vector3f.mul(i);
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
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		final FabricSpriteProvider PROVIDER;

		public Provider(FabricSpriteProvider provider) {
			PROVIDER = provider;
		}

		public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel world, double d, double e, double f, double g, double h, double i) {
			DecalParticle particle = new DecalParticle(world, d, e, f, g, h, i);
			particle.setSpriteFromAge(PROVIDER);
			return particle;
		}
	}
}
