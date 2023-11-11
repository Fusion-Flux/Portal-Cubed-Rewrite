package io.github.fusionflux.portalcubed.framework.shape;

import io.github.fusionflux.portalcubed.framework.util.TransformUtils;

import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Oriented Bounding Box
 */
public record OBB(AABB bounds, Quaternionf rotation) {
	public boolean contains(Vec3 pos) {
		Vec3 inBoxCoords = TransformUtils.apply(pos, rotation::transformInverse);
		return bounds.contains(inBoxCoords);
	}

	public boolean contains(Vector3f pos) {
		Vector3f transformed = rotation.transformInverse(pos, new Vector3f());
		return bounds.contains(transformed.x, transformed.y, transformed.z);
	}

	/**
	 * Check if this box contains the pos, but avoid allocating a new vector.
	 * Will mutate the provided one.
	 */
	@Contract(mutates = "param")
	public boolean containsFast(Vector3f pos) {
		rotation.transformInverse(pos);
		return bounds.contains(pos.x, pos.y, pos.z);
	}
}
