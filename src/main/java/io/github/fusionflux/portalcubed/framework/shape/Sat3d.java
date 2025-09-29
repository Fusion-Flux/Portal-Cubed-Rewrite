package io.github.fusionflux.portalcubed.framework.shape;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.util.DoubleRange;

/**
 * Implementation of the Separating Axis Theorem in 3 dimensions. Can handle arbitrary axes.
 */
public final class Sat3d {
	private Sat3d() {}

	/**
	 * @param shapeA the set of vertices defining the first shape
	 * @param shapeB the set of vertices defining the second shape
	 * @param axes the set of axes to test for separation along. Axes with length {@code 0} are allowed, but ignored.
	 * @return an offset to apply to {@code shapeB} to separate it from {@code shapeA}, or null if no collision occurred
	 */
	@Nullable
	public static Vector3d run(Iterable<Vector3dc> shapeA, Iterable<Vector3dc> shapeB, Iterator<Vector3dc> axes) {
		// the axis which the two shapes should be separated along
		Vector3dc separationAxis = null;
		// signed offset along the axis to separate the shapes
		double smallestOffset = Double.MAX_VALUE;

		while (axes.hasNext()) {
			Vector3dc axis = axes.next();
			if (axis.lengthSquared() == 0)
				continue;

			DoubleRange aRange = DoubleRange.project(axis, shapeA);
			DoubleRange bRange = DoubleRange.project(axis, shapeB);

			double offset = aRange.offsetFor(bRange);
			if (offset == 0) {
				// gap found, give up
				return null;
			}

			if (Math.abs(offset) < Math.abs(smallestOffset)) {
				smallestOffset = offset;
				separationAxis = axis;
			}
		}

		if (separationAxis == null)
			return null;

		return new Vector3d(separationAxis).mul(smallestOffset);
	}
}
