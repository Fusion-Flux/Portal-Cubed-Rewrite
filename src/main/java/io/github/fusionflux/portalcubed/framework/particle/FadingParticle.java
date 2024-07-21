package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public abstract class FadingParticle extends TextureSheetParticle {
	public float fadeStartLife = 0.5f;
	public boolean fadeSize = true;
	public boolean fadeAlpha = true;

	private float fadeProgress;

	protected FadingParticle(ClientLevel world, double x, double y, double z) {
		super(world, x, y, z);
	}

	protected FadingParticle(ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
		super(world, x, y, z, dx, dy, dz);
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		float life = Math.min((this.age + tickDelta) / this.lifetime, 1);
		this.fadeProgress = Math.max(Mth.inverseLerp(life, this.fadeStartLife, 1f), 0f);
		if (fadeAlpha)
			this.setAlpha(1f - this.fadeProgress);

		super.render(vertexConsumer, camera, tickDelta);
	}

	@Override
	public float getQuadSize(float tickDelta) {
		float quadSize = super.getQuadSize(tickDelta);
		return fadeSize ? quadSize * (1f - this.fadeProgress) : quadSize;
	}
}
