package io.github.fusionflux.portalcubed.framework.util;

import java.util.function.UnaryOperator;

import org.joml.Vector3d;

import net.minecraft.world.phys.Vec3;

public class TransformUtils {
	@SafeVarargs
	public static Vec3 apply(Vec3 input, UnaryOperator<Vector3d>... functions) {
        Vector3d result = new Vector3d(input.x, input.y, input.z);
		for (UnaryOperator<Vector3d> function : functions) {
			result = function.apply(result);
		}
		return new Vec3(result.x, result.y, result.z);
	}
}
