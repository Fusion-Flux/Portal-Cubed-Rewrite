package io.github.fusionflux.portalcubed.framework.shape;

import java.util.Iterator;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.util.DoubleRange;

/**
 * Implementation of the Separating Axis Theorem in 3 dimensions. Can handle arbitrary axes.
 */
public final class Sat3d {
	private Sat3d() {}

	@Nullable
	public static Vector3d run(Iterable<Vector3dc> shapeA, Iterable<Vector3dc> shapeB, Iterator<Vector3dc> axes) {
		double smallestDistanceOnAxis = Double.MAX_VALUE;
		Vector3dc smallestDistanceAxis = null;

		while (axes.hasNext()) {
			Vector3dc axis = axes.next();

			DoubleRange aRange = DoubleRange.project(axis, shapeA);
			DoubleRange bRange = DoubleRange.project(axis, shapeB);

			if (!aRange.intersects(bRange)) {
				// gap found, give up
				return null;
			}

			double overlap = -(bRange.max() - aRange.min());

			if (overlap < smallestDistanceOnAxis) {
				smallestDistanceOnAxis = overlap;
				smallestDistanceAxis = axis;
			}
		}

		if (smallestDistanceOnAxis >= 0)
			return null;

		Objects.requireNonNull(smallestDistanceAxis);

		return smallestDistanceAxis.mul(-smallestDistanceOnAxis, new Vector3d());
	}
}
