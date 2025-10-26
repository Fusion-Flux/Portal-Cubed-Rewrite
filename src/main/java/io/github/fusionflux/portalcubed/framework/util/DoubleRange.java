package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Vector2dc;
import org.joml.Vector3dc;

public record DoubleRange(double min, double max) {
	public DoubleRange shift(double value) {
		return new DoubleRange(this.min + value, this.max + value);
	}

	public DoubleRange expandTowards(double value) {
		if (value > 0) {
			return new DoubleRange(this.min, this.max + value);
		} else if (value < 0) {
			return new DoubleRange(this.min - value, this.max);
		} else {
			return this;
		}
	}

	public boolean intersects(DoubleRange that) {
		return this.intersects(that, 0);
	}

	public boolean intersects(DoubleRange that, double epsilon) {
		return !((this.min - that.max > epsilon) || (that.min - this.max > epsilon));
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

	public double midpoint() {
		return (this.max + this.min) / 2;
	}

	/**
	 * Calculates the offset required to move {@code that} out of {@code this}. This operation is asymmetrical.
	 * @return the offset, or 0 if there's no overlap
	 */
	public double offsetFor(DoubleRange that) {
		if (!this.intersects(that))
			return 0;

		if (this.midpoint() < that.midpoint()) {
			// offset should be positive (right)
			return this.max - that.min;
		} else {
			// offset should be negative (left)
			return this.min - that.max;
		}
	}

	/**
	 * @see #project(Vector3dc, Iterable)
	 */
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

	/**
	 * Project all the given vertices onto the given axis.
	 * @see #project(Vector2dc, Iterable) the 2d version
	 */
	public static DoubleRange project(Vector3dc axis, Iterable<Vector3dc> vertices) {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		for (Vector3dc vertex : vertices) {
			double dot = axis.dot(vertex);
			min = Math.min(min, dot);
			max = Math.max(max, dot);
		}

		return new DoubleRange(min, max);
	}
}
