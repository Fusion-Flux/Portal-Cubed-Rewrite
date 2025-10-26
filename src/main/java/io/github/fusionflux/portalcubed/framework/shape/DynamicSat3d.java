package io.github.fusionflux.portalcubed.framework.shape;

import java.util.Iterator;

import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.util.DoubleRange;
import io.github.fusionflux.portalcubed.framework.util.Maath;

/**
 * Variant of the Separating Axis Theorem that can handle one of the shapes moving. Can handle arbitrary axes.
 * <p>
 * Based on Real Time Collision Detection by Christer Ericson, section 5.5.8.
 */
public final class DynamicSat3d {
	private static final double noCollision = 1;
	// we need to allow a bit of a buffer for some things, or else perfectly float-aligned shapes will count as colliding
	private static final double leniency = 1e-7;

	private DynamicSat3d() {}

	/**
	 * @param staticShape the set of vertices defining the first shape
	 * @param motion the motion of {@code shapeA} relative to {@code shapeB}
	 * @param movingShape the set of vertices defining the second shape
	 * @param axes the set of axes to test for separation along. Axes with length {@code 0} are allowed, but ignored.
	 * @return a normalized scalar to multiply {@code motion} by to find the maximum allowed motion before colliding, or -1 if a collision is already occurring
	 */
	public static double run(Iterable<Vector3dc> staticShape, Iterable<Vector3dc> movingShape, Vector3dc motion, Iterator<Vector3dc> axes) {
		if (Maath.isZero(motion)) {
			throw new IllegalArgumentException("Motion cannot be 0");
		}

		// earliest and latest times when the shapes are overlapping
		double tFirst = 0;
		double tLast = 1;

		while (axes.hasNext()) {
			Vector3dc axis = axes.next();
			if (Maath.isZero(axis))
				continue;

			DoubleRange staticRange = DoubleRange.project(axis, staticShape);
			double aMin = staticRange.min();
			double aMax = staticRange.max();

			DoubleRange movingRange = DoubleRange.project(axis, movingShape);
			double bMin = movingRange.min();
			double bMax = movingRange.max();

			double v = axis.dot(motion);

			// imagine a line in your mind. it's free and the cops can't stop you.
			// negative positions are to the left on this line, and positive ones are to the right.
			// the shapes are projected onto the line as ranges. their bounds are currently unknown.

			if (v > 0) {
				// shape B is moving to the right
				if (bMin >= aMax - leniency) {
					// shape B's start is to the right of shape A's end, so they cannot possibly collide
					return noCollision;
				}

				if (bMax < aMin) {
					// shape B's end is to the left of shape A's start, so there is a time in the future where they will collide
					tFirst = Math.max((aMin - bMax) / v, tFirst);
				}

				if (aMax < bMin) {
					// shape A's end is to the left of shape B's start, so there is a time in the future where they will collide
					tLast = Math.min((aMax - bMin) / v, tLast);
				}
			} else if (v < 0) {
				// shape B is moving to the left
				if (bMax <= aMin + leniency) {
					// shape B's end is to the left of shape A's start, so they cannot possibly collide
					return noCollision;
				}

				if (aMax < bMin) {
					// shape A's end is to the left of shape B's start, so there is a time in the future where they will collide
					tFirst = Math.max((aMax - bMin) / v, tFirst);
				}
				if (bMax > aMin) {
					// shape B's end is to the right of shape A's start, so there is a time in the future where they will collide
					tLast = Math.min((aMin - bMax) / v, tLast);
				}
			} else {
				// no motion on this axis, but it's possible that the shapes are separated here.
				// this is not handled in RTCD for some reason, likely due to the fact it's only handling AABBs.
				if (!staticRange.intersects(movingRange, -leniency)) {
					return noCollision;
				}
			}

			if (tFirst > tLast) {
				return noCollision;
			}
		}

		// it's possible that the calculated time of collision is in the far future (>1, farther than the goal)
		return Math.min(tFirst, 1);
	}
}
