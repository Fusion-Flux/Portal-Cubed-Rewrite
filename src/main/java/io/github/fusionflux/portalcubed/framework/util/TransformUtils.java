package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TransformUtils {
	/**
	 * Projects A onto B
	 */
	public static Vec3 project(Vec3 a, Vec3 b) {
		double length = a.dot(b) / (b.length() * b.length());
		return b.scale(length);
	}

	/**
	 * Create a reusable iterator that provides the vertices of the given box.
	 * Note that returned vertices should never be retained, as the returned object may be mutated in the future.
	 */
	public static Iterable<Vector3dc> vertices(AABB box) {
		Vector3d scratch = new Vector3d();
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> scratch.set(box.minX, box.minY, box.minZ);
			case 1 -> scratch.set(box.minX, box.minY, box.maxZ);
			case 2 -> scratch.set(box.minX, box.maxY, box.minZ);
			case 3 -> scratch.set(box.minX, box.maxY, box.maxZ);
			case 4 -> scratch.set(box.maxX, box.minY, box.minZ);
			case 5 -> scratch.set(box.maxX, box.minY, box.maxZ);
			case 6 -> scratch.set(box.maxX, box.maxY, box.minZ);
			case 7 -> scratch.set(box.maxX, box.maxY, box.maxZ);
			default -> null;
		});
	}

	public static Vec3 withLength(Vec3 vec, double length) {
		return vec.normalize().scale(length);
	}

	public static boolean equals(Quaternionfc a, Quaternionfc b, float delta) {
		return a.equals(b, delta) || a.equals(b.mul(-1, new Quaternionf()), delta);
	}

	public static Vector3d toJoml(Vec3 vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}

	public static Vec3 toMc(Vector3dc vec) {
		return new Vec3(vec.x(), vec.y(), vec.z());
	}
}
