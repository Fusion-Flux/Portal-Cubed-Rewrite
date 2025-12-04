package io.github.fusionflux.portalcubed.content.portal.graphics;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticle;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;

public class PortalProjectileParticle extends CustomTrailParticle {
	public PortalProjectileParticle(ClientLevel level, double x, double y, double z, Vec3 target, int color, int duration) {
		super(level, x, y, z, target, color, duration);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getLightColor(float tint) {
		return LightTexture.FULL_BRIGHT;
	}

	public record Provider(FabricSpriteProvider spriteProvider) implements ParticleProvider<CustomTrailParticleOption> {
		@NotNull
		@Override
		public Particle createParticle(CustomTrailParticleOption particleOption, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			PortalProjectileParticle particle = new PortalProjectileParticle(world, x, y, z, particleOption.target(), particleOption.color(), particleOption.duration());
			particle.pickSprite(this.spriteProvider);
			return particle;
		}
	}
}
