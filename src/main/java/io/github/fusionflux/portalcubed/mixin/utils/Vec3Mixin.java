package io.github.fusionflux.portalcubed.mixin.utils;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import net.minecraft.world.phys.Vec3;

@Mixin(Vec3.class)
public class Vec3Mixin implements Vec3Ext {
	@Shadow
	@Final
	public double x;
	@Shadow
	@Final
	public double y;
	@Shadow
	@Final
	public double z;

	@Unique
	private Vector3dc jomlVector;

	@Override
	public Vector3dc asJoml() {
		// this is technically a race condition but it's harmless
		if (this.jomlVector == null) {
			this.jomlVector = new Vector3d(this.x, this.y, this.z);
		}

		return this.jomlVector;
	}
}
