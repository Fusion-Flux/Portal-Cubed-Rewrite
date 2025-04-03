package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.List;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2dc;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.framework.shape.flat.Line2d;
import net.minecraft.world.phys.Vec3;

/**
 * @param origin the point in 3d space corresponding to the 2d origin of this surface
 * @param supportsPortalRotation true if portals on this surface may be rotated around this surface's normal axis
 */
public record PortalableSurface(Quaternionfc rotation, Vec3 origin, List<Line2d> walls, boolean supportsPortalRotation) {
	public PortalableSurface(Quaternionf rotation, List<Line2d> walls, boolean supportsPortalRotation) {
		this(rotation, Vec3.ZERO, walls, supportsPortalRotation);
	}

	public Vec3 to3d(Vector2dc pos) {
		Vector3d up = this.rotation.transform(new Vector3d(0, 0, 1));
		Vector3d right = this.rotation.transform(new Vector3d(1, 0, 0));

		up.mul(pos.y());
		right.mul(pos.x());
		up.add(right);
		return new Vec3(up.x, up.y, up.z).add(this.origin);
	}
}
