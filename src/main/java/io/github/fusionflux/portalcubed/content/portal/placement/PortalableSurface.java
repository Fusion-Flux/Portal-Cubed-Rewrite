package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.List;

import org.joml.Quaternionfc;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.framework.shape.flat.Line2d;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.world.phys.Vec3;

/**
 * @param origin the point in 3d space corresponding to the 2d origin of this surface
 * @param supportsPortalRotation true if portals on this surface may be rotated around this surface's normal axis
 * @param containedPoint any point that is known to be valid on this surface
 */
public record PortalableSurface(Quaternionfc rotation, Vec3 origin, Vector2dc containedPoint, List<Line2d> walls, boolean supportsPortalRotation) {

	public boolean contains(Vector2dc point) {
		// if the number of intersections is divisible by 2, then the point is contained
		Line2d path = new Line2d(this.containedPoint, point);
		// DebugRendering.addLine(100, path.to3d(this).moved(new Vec3(0, 1, 0)), Color.RED);
		int intersections = 0;
		for (Line2d wall : this.walls) {
			if (wall.intersects(path)) {
				// DebugRendering.addLine(100, wall.to3d(this).moved(new Vec3(0, 1, 0)), Color.ORANGE);
				intersections++;
			}
		}
		return intersections % 2 == 0;
	}

	public boolean intersectsCollision(Line2d path) {
		for (Line2d wall : this.walls) {
			if (wall.source() == Line2d.Source.COLLISION && wall.intersects(path)) {
				return true;
			}
		}
		return false;
	}

	public Vec3 to3d(Vector2dc pos) {
		Vector3d up = this.rotation.transform(new Vector3d(0, 0, 1));
		Vector3d right = this.rotation.transform(new Vector3d(1, 0, 0));

		up.mul(pos.y());
		right.mul(pos.x());
		up.add(right);
		return new Vec3(up.x, up.y, up.z).add(this.origin);
	}

	public Vector2d to2d(Vec3 pos) {
		Vec3 up = TransformUtils.toMc(this.rotation.transform(new Vector3d(0, 0, 1)));
		Vec3 right = TransformUtils.toMc(this.rotation.transform(new Vector3d(1, 0, 0)));

		Vec3 relative = this.origin.vectorTo(pos);

		Vec3 xPos = relative.projectedOn(right);
		double x = allSignsMatch(xPos, right) ? xPos.length() : -xPos.length();
		Vec3 yPos = relative.projectedOn(up);
		double y = allSignsMatch(yPos, up) ? yPos.length() : -yPos.length();
		return new Vector2d(x, y);
	}

	private static boolean allSignsMatch(Vec3 a, Vec3 b) {
		return Math.signum(a.x) == Math.signum(b.x)
				&& Math.signum(a.y) == Math.signum(b.y)
				&& Math.signum(a.z) == Math.signum(b.z);
	}
}
