package io.github.fusionflux.portalcubed.content.portal.placement;

import org.joml.Matrix2d;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.framework.shape.flat.Line2d;
import io.github.fusionflux.portalcubed.framework.util.Angle;
import io.github.fusionflux.portalcubed.framework.util.DoubleRange;
import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;

public record PortalCandidate(Angle rot, Vector2dc center,
							  Vector2dc bottomLeft, Vector2dc bottomRight, Vector2dc topRight, Vector2dc topLeft,
							  Line2d bottom, Line2d right, Line2d top, Line2d left) {

	public PortalCandidate(Angle rot, Vector2dc center, Vector2dc bottomLeft, Vector2dc bottomRight, Vector2dc topRight, Vector2dc topLeft) {
		this(
				rot, center,
				bottomLeft, bottomRight, topRight, topLeft,
				new Line2d(bottomLeft, bottomRight), new Line2d(bottomRight, topRight),
				new Line2d(topRight, topLeft), new Line2d(topLeft, bottomLeft)
		);
	}

	public PortalCandidate moved(double x, double y) {
		return new PortalCandidate(
				this.rot, add(this.center, x, y),
				add(this.bottomLeft, x, y), add(this.bottomRight, x, y), add(this.topRight, x, y), add(this.topLeft, x, y)
		);
	}

	public DoubleRange project(Vector2dc axis) {
		return DoubleRange.project(axis, this.vertices());
	}

	public Iterable<Line2d> lines() {
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> this.bottom;
			case 1 -> this.right;
			case 2 -> this.top;
			case 3 -> this.left;
			default -> null;
		});
	}

	public Iterable<Vector2dc> vertices() {
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> this.bottomLeft;
			case 1 -> this.bottomRight;
			case 2 -> this.topRight;
			case 3 -> this.topLeft;
			default -> null;
		});
	}

	public static PortalCandidate create(Vector2dc center, double width, double height, Angle rot) {
		Matrix2d rotationMatrix = new Matrix2d().rotation(rot.rad());

		double hw = width / 2;
		double hh = height / 2;

		Vector2dc bottomLeft = rotationMatrix.transform(new Vector2d(-hw, -hh)).add(center);
		Vector2dc bottomRight = rotationMatrix.transform(new Vector2d(hw, -hh)).add(center);
		Vector2dc topRight = rotationMatrix.transform(new Vector2d(hw, hh)).add(center);
		Vector2dc topLeft = rotationMatrix.transform(new Vector2d(-hw, hh)).add(center);

		return new PortalCandidate(rot, center, bottomLeft, bottomRight, topRight, topLeft);
	}

	public static PortalCandidate initial(Angle rot) {
		return create(new Vector2d(), PortalInstance.WIDTH, PortalInstance.HEIGHT, rot);
	}

	public static PortalCandidate other(Vector2dc origin, Angle rot) {
		return create(origin, PortalInstance.WIDTH - 0.01, PortalInstance.HEIGHT - 0.01, rot);
	}

	private static Vector2d add(Vector2dc vec, double x, double y) {
		return vec.add(x, y, new Vector2d());
	}
}
