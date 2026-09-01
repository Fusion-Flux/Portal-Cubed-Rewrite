package io.github.fusionflux.portalcubed.content.portal.graphics;

import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticle;
import io.github.fusionflux.portalcubed.framework.particle.CustomTrailParticleOption;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class PortalProjectileParticle extends CustomTrailParticle {
	public PortalProjectileParticle(ClientLevel level, double x, double y, double z, Vec3 target, int color, int duration, TextureAtlasSprite sprite) {
		super(level, x, y, z, target, color, duration, sprite);
	}

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	@Override
	protected int getLightCoords(float a) {
		return LightCoordsUtil.FULL_BRIGHT;
	}

	public record Provider(FabricSpriteSet spriteSet) implements ParticleProvider<CustomTrailParticleOption> {
		@Override
		public Particle createParticle(CustomTrailParticleOption options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
			return new PortalProjectileParticle(level, x, y, z, options.target(), options.color(), options.duration(), this.spriteSet.get(random));
		}
	}
}
