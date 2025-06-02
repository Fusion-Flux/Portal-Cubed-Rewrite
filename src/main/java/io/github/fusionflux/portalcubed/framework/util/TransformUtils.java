package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import net.minecraft.world.phys.Vec3;

public class TransformUtils {
	/**
	 * Projects A onto B
	 */
	public static Vec3 project(Vec3 a, Vec3 b) {
		double length = a.dot(b) / (b.length() * b.length());
		return b.scale(length);
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
