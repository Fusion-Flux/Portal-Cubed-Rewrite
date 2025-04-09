package io.github.fusionflux.portalcubed.framework.shape.flat;

import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.placement.PortalableSurface;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.util.DoubleRange;
import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;
import net.minecraft.util.Mth;

public record Line2d(Vector2dc from, Vector2dc to) {
	public Line2d flip() {
		return new Line2d(this.to, this.from);
	}

	public double distanceOf(Vector2dc point) {
		return Intersectiond.distancePointLine(
				point.x(), point.y(),
				this.from.x(), this.from.y(),
				this.to.x(), this.to.y()
		);
	}

	public boolean intersects(Line2d that) {
		double distance = Intersectiond.findClosestPointsLineSegments(
				this.from.x(), this.from.y(), 0, this.to.x(), this.to.y(), 0,
				that.from.x(), that.from.y(), 0, that.to.x(), that.to.y(), 0,
				// send the actual positions to the void.
				// can't reuse a single one here, since joml reads their values after setting them
				new Vector3d(), new Vector3d()
		);

		// if this precision is too low (ex. 1e-5, like in Mth.equal) then intersections can be too lenient
		return Math.abs(distance) < 1e-7;
	}

	public boolean isAlignedWith(Line2d other) {
		return Mth.equal(this.distanceOf(other.from), 0) && Mth.equal(this.distanceOf(other.to), 0);
	}

	public Vector2d perpendicularCcwAxis() {
		Vector2d axis = this.axis();
		//noinspection SuspiciousNameCombination - intended component swap
		return axis.set(-axis.y, axis.x);
	}

	public Vector2d axis() {
		return this.to.sub(this.from, new Vector2d()).normalize();
	}

	public DoubleRange project(Vector2dc axis) {
		return DoubleRange.project(axis, this.vertices());
	}

	public Iterable<Vector2dc> vertices() {
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> this.from;
			case 1 -> this.to;
			default -> null;
		});
	}

	public Vector2d midpoint() {
		return this.from.lerp(this.to, 0.5, new Vector2d());
	}

	public Line to3d(PortalableSurface surface) {
		return new Line(surface.to3d(this.from), surface.to3d(this.to));
	}
}
