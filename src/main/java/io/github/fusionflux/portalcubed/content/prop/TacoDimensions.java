package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;

public class TacoDimensions extends EntityDimensions {
	public TacoDimensions() {
		super(.69375f, .38125f, true);
	}

	@Override
	public AABB makeBoundingBox(double x, double y, double z) {
		float width =  this.width / 2f;
		float height = this.height;
		float length = .19375f / 2f;
		return new AABB(x - width, y, z - length, x + width, y + height, z + length);
	}
}
