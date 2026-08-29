package io.github.fusionflux.portalcubed.framework.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public abstract class CustomTrailParticle extends SingleQuadParticle {
	protected CustomTrailParticle(ClientLevel level, double x, double y, double z, Vec3 target, int color, int duration, TextureAtlasSprite sprite) {
		super(level, x, y, z, sprite);

		Vec3 vel = target.subtract(x, y, z).scale(1d / duration);
		this.xd = vel.x;
		this.yd = vel.y;
		this.zd = vel.z;
		this.lifetime = duration;

		this.quadSize = .5f;
		this.rCol = ARGB.redFloat(color);
		this.gCol = ARGB.greenFloat(color);
		this.bCol = ARGB.blueFloat(color);
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age >= this.lifetime) {
			this.remove();
		} else {
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
		}
		this.age++;
	}

	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {
		this.setAlpha(Math.min((this.age + partialTickTime) * 0.15f, 1));
		super.extract(particleTypeRenderState, camera, partialTickTime);
	}
}
