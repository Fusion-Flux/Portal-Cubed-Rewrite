package io.github.fusionflux.portalcubed.framework.shape.flat;

import org.joml.Vector2d;
import org.joml.Vector2dc;

import io.github.fusionflux.portalcubed.content.portal.placement.PortalableSurface;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.util.DoubleRange;
import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;

public record Line2d(Vector2dc from, Vector2dc to) {

	public Vector2d perpendicularCcwAxis() {
		Vector2d offset = this.to.sub(this.from, new Vector2d());
		//noinspection SuspiciousNameCombination - intended component swap
		return offset.set(-offset.y, offset.x).normalize();
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
