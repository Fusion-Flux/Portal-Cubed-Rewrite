package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Vector2dc;

/**
 * Both ends are exclusive
 */
public record DoubleRange(double min, double max) {
	public DoubleRange add(double value) {
		return new DoubleRange(this.min + value, this.max + value);
	}

	public static DoubleRange project(Vector2dc axis, Iterable<Vector2dc> vertices) {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		for (Vector2dc vertex : vertices) {
			double dot = axis.dot(vertex);
			min = Math.min(min, dot);
			max = Math.max(max, dot);
		}

		return new DoubleRange(min, max);
	}
}
