package io.github.fusionflux.portalcubed.content.portal;

import org.jetbrains.annotations.NotNull;

import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticle;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.phys.Vec3;

public class PortalProjectileParticle extends CustomTrailParticle {
	public PortalProjectileParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Vec3 target, int color) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, target, color);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public record Provider(FabricSpriteProvider spriteProvider) implements ParticleProvider<CustomTrailParticleOption> {
		@NotNull
		@Override
		public Particle createParticle(CustomTrailParticleOption particleOption, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
			PortalProjectileParticle particle = new PortalProjectileParticle(world, x, y, z, dx, dy, dz, particleOption.target(), particleOption.color());
			particle.setLifetime(particleOption.duration());
			particle.pickSprite(this.spriteProvider);
			return particle;
		}
	}
}
