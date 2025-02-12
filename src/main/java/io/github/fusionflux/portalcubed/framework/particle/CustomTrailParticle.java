package io.github.fusionflux.portalcubed.framework.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public abstract class CustomTrailParticle extends TextureSheetParticle {
	protected CustomTrailParticle(ClientLevel level, double x, double y, double z, Vec3 target, int color, int duration) {
		super(level, x, y, z);

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
	public void render(VertexConsumer buffer, Camera camera, float tickDelta) {
		this.alpha = Math.min(this.age - tickDelta, 1);
		if (this.alpha > 0)
			super.render(buffer, camera, tickDelta);
	}
}
