package io.github.fusionflux.portalcubed.framework.util;

import java.util.function.UnaryOperator;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;

import net.minecraft.util.Mth;

import org.joml.Quaternionf;
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

	public static Quaternionf quaternionOf(FrontAndTop orientation) {
		Direction top = orientation.top();
		Direction front = orientation.front();
		float topAngle = 0;
		if (front.getAxis().isVertical()) {
			top = (front == Direction.DOWN && top.getAxis() == Direction.Axis.Z) ? top.getOpposite() : top;
			topAngle = Mth.DEG_TO_RAD * switch (top) {
				case NORTH -> 180;
				case WEST -> 90;
				case EAST -> -90;
				default -> 0;
			};
		}
		Quaternionf rotation = new Quaternionf();
		switch (front) {
			case DOWN -> rotation.rotationXYZ(Mth.DEG_TO_RAD * 270, 0, topAngle);
			case UP -> rotation.rotationXYZ(Mth.DEG_TO_RAD *  -270, 0, topAngle);
			case SOUTH -> rotation.rotationY(Mth.DEG_TO_RAD *  180);
			case WEST -> rotation.rotationY(Mth.DEG_TO_RAD *   90);
			case EAST -> rotation.rotationY(Mth.DEG_TO_RAD *  -90);
		}
		return rotation;
	}

	public static Quaternionf rotateAround(Quaternionf rotation, Direction.Axis axis, float deg) {
		float rot = Mth.DEG_TO_RAD * deg;
		return switch (axis) {
			case X -> rotation.rotateX(rot, new Quaternionf());
			case Y -> rotation.rotateY(rot, new Quaternionf());
			case Z -> rotation.rotateZ(rot, new Quaternionf());
		};
	}
}