package io.github.fusionflux.portalcubed.framework.shape;

import java.util.List;

import io.github.fusionflux.portalcubed.framework.util.Quad;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;

import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Oriented Bounding Box
 */
public record OBB(Vec3 center, double width, double height, double depth, Quaternionf rotation) {
	public boolean contains(Vec3 pos) {
		Vec3 inBoxCoords = TransformUtils.apply(pos, rotation::transformInverse);
		return bounds.contains(inBoxCoords);
	}

	public boolean contains(Vector3f pos) {
		Vector3f transformed = rotation.transformInverse(pos, new Vector3f());
		return bounds.contains(transformed.x, transformed.y, transformed.z);
	}

	public boolean intersects(AABB aabb) {

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

	public List<BlockPos> intersectingBlocks() {

	}

	public AABB encompassingAabb() {

	}

	public static OBB extrudeQuad(Quad quad, double depth) {
		Vec3 quadCenter = quad.center();
		Vec3 normal = quad.normal();
		Vec3 offsetToCenter = normal.scale(depth / 2);
		Vec3 center = quadCenter.add(offsetToCenter);

		Quaternionf rotation = new Quaternionf();
		// TODO get rotation from quad
		return new OBB(center, quad.width(), quad.height(), depth, rotation);
	}
}
