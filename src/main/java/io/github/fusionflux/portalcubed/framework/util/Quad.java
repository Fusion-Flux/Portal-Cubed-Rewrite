package io.github.fusionflux.portalcubed.framework.util;

import com.google.common.collect.Iterables;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record Quad(Tri a, Tri b) {
	@Nullable
	public Vec3 clip(Vec3 from, Vec3 to) {
		Vec3 clipA = a.clip(from, to);
		if (clipA != null)
			return clipA;
		return b.clip(from, to);
	}

	public Iterable<Vec3> vertices() {
		return Iterables.concat(this.a, this.b);
	}

	public AABB containingBox() {
		double minX = a.a().x;
		double minY = a.a().y;
		double minZ = a.a().z;
		double maxX = a.a().x;
		double maxY = a.a().y;
		double maxZ = a.a().z;

		for (Vec3 vertex : this.vertices()) {
			minX = Math.min(minX, vertex.x);
			minY = Math.min(minY, vertex.y);
			minZ = Math.min(minZ, vertex.z);
			maxX = Math.max(maxX, vertex.x);
			maxY = Math.max(maxY, vertex.y);
			maxZ = Math.max(maxZ, vertex.z);
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static Quad create(Quaternionf rotation, Vec3 center, double width, double height) {
		// relative offsets
		Vec3 topRight = transform(rotation, new Vec3(width, 0, height));
		Vec3 topLeft = transform(rotation, new Vec3(-width, 0, height));
		Vec3 bottomRight = transform(rotation, new Vec3(width, 0, -height));
		Vec3 bottomLeft = transform(rotation, new Vec3(-width, 0, -height));
		// de-relativize
		topRight = center.add(topRight);
		topLeft = center.add(topLeft);
		bottomRight = center.add(bottomRight);
		bottomLeft = center.add(bottomLeft);

		Tri a = new Tri(topLeft, topRight, bottomLeft);
		Tri b = new Tri(topRight, bottomRight, bottomLeft);

		return new Quad(a, b);
	}

	private static Vec3 transform(Quaternionf rotation, Vec3 vec) {
		Vector3d joml = new Vector3d(vec.x, vec.y, vec.z);
		rotation.transform(joml);
		return new Vec3(joml.x, joml.y, joml.z);
	}
}
