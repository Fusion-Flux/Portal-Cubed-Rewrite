package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Vector2dc;

public record DoubleRange(double min, double max) {
	public DoubleRange add(double value) {
		return new DoubleRange(this.min + value, this.max + value);
	}

	public boolean intersects(DoubleRange other) {
		return !((this.min - other.max > 0) || (other.min - this.max > 0));
	}

	public boolean contains(DoubleRange other) {
		return this.min <= other.min && this.max >= other.max;
	}

	public boolean contains(double value) {
		return value > this.min && value < this.max;
	}

	public double size() {
		return this.max - this.min;
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
