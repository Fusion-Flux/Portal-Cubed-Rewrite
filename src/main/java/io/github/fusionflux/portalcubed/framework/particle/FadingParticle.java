package io.github.fusionflux.portalcubed.framework.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

public abstract class FadingParticle extends SingleQuadParticle {
	public float fadeStartLife = 0.5f;
	public boolean fadeSize = true;
	public boolean fadeAlpha = true;

	private float fadeProgress;

	protected FadingParticle(ClientLevel world, double x, double y, double z, TextureAtlasSprite sprite) {
		super(world, x, y, z, sprite);
	}

	protected FadingParticle(ClientLevel world, double x, double y, double z, double dx, double dy, double dz, TextureAtlasSprite sprite) {
		super(world, x, y, z, dx, dy, dz, sprite);
	}

	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {
		float life = Math.min((this.age + partialTickTime) / this.lifetime, 1);
		this.fadeProgress = Math.max(Mth.inverseLerp(life, this.fadeStartLife, 1f), 0f);
		if (this.fadeAlpha)
			this.setAlpha(1f - this.fadeProgress);

		super.extract(particleTypeRenderState, camera, partialTickTime);
	}

	@Override
	public float getQuadSize(float tickDelta) {
		float quadSize = super.getQuadSize(tickDelta);
		return fadeSize ? quadSize * (1f - this.fadeProgress) : quadSize;
	}
}
