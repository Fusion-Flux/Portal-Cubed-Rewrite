package io.github.fusionflux.portalcubed.framework.shape;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;

import com.google.common.collect.Iterables;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
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

	public Vec3 topLeft() {
		return this.a.c();
	}

	public Vec3 topRight() {
		return this.a.b();
	}

	public Vec3 bottomLeft() {
		return this.a.a();
	}

	public Vec3 bottomRight() {
		return this.b.b();
	}

	public Vec3 normal() {
		// assume not degenerate
		return this.a.normal();
	}

	public Vec3 up() {
		return this.bottomRight().vectorTo(this.topRight()).normalize();
	}

	public Vec3 right() {
		return this.topLeft().vectorTo(this.topRight()).normalize();
	}

	public Vec3 center() {
		return this.topLeft().lerp(this.bottomRight(), 0.5);
	}

	public double width() {
		return this.topLeft().distanceTo(this.topRight());
	}

	public double height() {
		return this.topLeft().distanceTo(this.bottomLeft());
	}

	public static Quad create(Quaternionfc rotation, Vec3 center, double width, double height) {
		double w = width / 2;
		double h = height / 2;
		// relative offsets
		Vec3 topRight = transform(rotation, new Vec3(-w, 0, h));
		Vec3 topLeft = transform(rotation, new Vec3(w, 0, h));
		Vec3 bottomRight = transform(rotation, new Vec3(-w, 0, -h));
		Vec3 bottomLeft = transform(rotation, new Vec3(w, 0, -h));
		// de-relativize
		topRight = center.add(topRight);
		topLeft = center.add(topLeft);
		bottomRight = center.add(bottomRight);
		bottomLeft = center.add(bottomLeft);

		// CCW winding order
		Tri a = new Tri(bottomLeft, topRight, topLeft);
		Tri b = new Tri(bottomLeft, bottomRight, topRight);

		return new Quad(a, b);
	}

	public static Quad create(Plane plane, double size) {
		Quaternionf rotation = new Quaternionf();
		rotation.rotateTo(PortalInstance.BASE_NORMAL.toVector3f(), plane.normal().toVector3f());
		return create(rotation, plane.origin(), size, size);
	}

	private static Vec3 transform(Quaternionfc rotation, Vec3 vec) {
		Vector3d joml = new Vector3d(vec.x, vec.y, vec.z);
		rotation.transform(joml);
		return new Vec3(joml.x, joml.y, joml.z);
	}
}
