package io.github.fusionflux.portalcubed.framework.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class CustomTrailParticle extends TextureSheetParticle {
	private final Vec3 target;

	protected CustomTrailParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Vec3 target, int color) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed);
		this.quadSize = .5f;
		this.rCol = ARGB.redFloat(color);
		this.gCol = ARGB.greenFloat(color);
		this.bCol = ARGB.blueFloat(color);
		this.target = target;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age >= this.lifetime) {
			this.remove();
		} else {
			double step = 1d / (this.lifetime - this.age);
			this.x = Mth.lerp(step, this.x, this.target.x());
			this.y = Mth.lerp(step, this.y, this.target.y());
			this.z = Mth.lerp(step, this.z, this.target.z());
		}
		this.age++;
	}
}
