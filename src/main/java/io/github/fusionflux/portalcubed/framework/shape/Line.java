package io.github.fusionflux.portalcubed.framework.shape;

import net.minecraft.world.phys.Vec3;

public record Line(Vec3 from, Vec3 to) {
	public Line moved(Vec3 offset) {
		return new Line(this.from.add(offset), this.to.add(offset));
	}
}
