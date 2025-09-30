package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Mojang removed the A, we're adding an extra one
 */
public final class Maath {
	public static double projectionLength(Vector3dc a, Vector3dc b) {
		return a.dot(b) / b.lengthSquared();
	}

	public static double get(Vector3dc vec, Direction.Axis axis) {
		return switch (axis) {
			case X -> vec.x();
			case Y -> vec.y();
			case Z -> vec.z();
		};
	}

	public static void set(Vector3d vec, Direction.Axis axis, double value) {
		switch (axis) {
			case X -> vec.x = value;
			case Y -> vec.y = value;
			case Z -> vec.z = value;
		}
	}

	public static AABB move(AABB box, Direction.Axis axis, double distance) {
		return switch (axis) {
			case X -> box.move(distance, 0, 0);
			case Y -> box.move(0, distance, 0);
			case Z -> box.move(0, 0, distance);
		};
	}

	public static Vector3d vectorOf(Direction.Axis axis, double value) {
		Vector3d vec = new Vector3d();
		set(vec, axis, value);
		return vec;
	}
}
