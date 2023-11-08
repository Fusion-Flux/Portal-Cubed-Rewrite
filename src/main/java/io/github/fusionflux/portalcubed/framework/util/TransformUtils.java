package io.github.fusionflux.portalcubed.framework.util;

import java.util.function.UnaryOperator;

import org.joml.Vector3d;

import net.minecraft.world.phys.Vec3;

public class TransformUtils {
	public static Vec3 apply(UnaryOperator<Vector3d> function, Vec3 input) {
		Vector3d asJomlVec = new Vector3d(input.x, input.y, input.z);
		Vector3d result = function.apply(asJomlVec);
		return new Vec3(result.x, result.y, result.z);
	}

	public static Vec3 applyDual(UnaryOperator<Vector3d> func1, UnaryOperator<Vector3d> func2, Vec3 input) {
		Vector3d asJomlVec = new Vector3d(input.x, input.y, input.z);
		Vector3d result = func2.apply(func1.apply(asJomlVec));
		return new Vec3(result.x, result.y, result.z);
	}
}
