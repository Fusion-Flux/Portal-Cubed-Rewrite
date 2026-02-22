package io.github.fusionflux.portalcubed.content.portal.ref;

import net.minecraft.world.phys.Vec3;

/**
 * A portal and a position on its surface, typically one hit by a raycast.
 */
public record HitPortal(PortalReference reference, Vec3 pos) {
	@Override
	public String toString() {
		return this.reference.toString() + " @ " + this.pos;
	}

	public static HitPortal ofCenter(PortalReference portal) {
		return new HitPortal(portal, portal.get().origin());
	}
}
