package io.github.fusionflux.portalcubed.framework.shape;

import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record Quad(Vector3dc center,
				   Vector3dc right, Vector3dc up, Vector3dc normal,
				   Vector3dc bottomLeft, Vector3dc bottomRight, Vector3dc topRight, Vector3dc topLeft) {
	public static final Vector3dc BASE_RIGHT = new Vector3d(1, 0, 0);
	public static final Vector3dc BASE_UP = new Vector3d(0, 0, 1);
	public static final Vector3dc BASE_NORMAL = new Vector3d(0, 1, 0);

	@Nullable
	public Vec3 clip(Vec3 from, Vec3 to) {
		Vector3d pos = new Vector3d();

		boolean intersect = Intersectiond.intersectLineSegmentTriangle(
				from.x, from.y, from.z,
				to.x, to.y, to.z,
				this.bottomLeft.x(), this.bottomLeft.y(), this.bottomLeft.z(),
				this.topRight.x(), this.topRight.y(), this.topRight.z(),
				this.topLeft.x(), this.topLeft.y(), this.topLeft.z(),
				1e-5,
				pos
		);
		if (!intersect) {
			intersect = Intersectiond.intersectLineSegmentTriangle(
					from.x, from.y, from.z,
					to.x, to.y, to.z,
					this.bottomLeft.x(), this.bottomLeft.y(), this.bottomLeft.z(),
					this.bottomRight.x(), this.bottomRight.y(), this.bottomRight.z(),
					this.topRight.x(), this.topRight.y(), this.topRight.z(),
					1e-5,
					pos
			);
		}

		return intersect ? TransformUtils.toMc(pos) : null;
	}

	public Iterable<Vector3dc> vertices() {
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> this.bottomLeft;
			case 1 -> this.bottomRight;
			case 2 -> this.topRight;
			case 3 -> this.topLeft;
			default -> null;
		});
	}

	public Iterable<Line> lines() {
		return () -> SimpleIterator.create(i -> switch (i) {
			case 0 -> new Line(this.bottomLeft, this.bottomRight);
			case 1 -> new Line(this.bottomRight, this.topRight);
			case 2 -> new Line(this.topRight, this.topLeft);
			case 3 -> new Line(this.topLeft, this.bottomLeft);
			default -> null;
		});
	}

	public boolean intersects(AABB box) {
		Vec3 boxCenter = box.getCenter();
		return Intersectiond.testObOb(
				this.center.x(), this.center.y(), this.center.z(),
				this.right.x(), this.right.y(), this.right.z(),
				this.up.x(), this.up.y(), this.up.z(),
				this.normal.x(), this.normal.y(), this.normal.z(),
				this.width() / 2, this.height() / 2, 0,
				boxCenter.x, boxCenter.y, boxCenter.z,
				1, 0, 0,
				0, 1, 0,
				0, 0, 1,
				box.getXsize() / 2, box.getYsize() / 2, box.getZsize() / 2
		);
	}

	public AABB containingBox() {
		double minX = this.bottomLeft.x();
		double minY = this.bottomLeft.y();
		double minZ = this.bottomLeft.z();
		double maxX = minX;
		double maxY = minY;
		double maxZ = minZ;

		for (Vector3dc vertex : this.vertices()) {
			minX = Math.min(minX, vertex.x());
			minY = Math.min(minY, vertex.y());
			minZ = Math.min(minZ, vertex.z());
			maxX = Math.max(maxX, vertex.x());
			maxY = Math.max(maxY, vertex.y());
			maxZ = Math.max(maxZ, vertex.z());
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public double width() {
		return this.topLeft().distance(this.topRight());
	}

	public double height() {
		return this.topLeft().distance(this.bottomLeft());
	}

	public static Quad create(Vector3dc center, double width, double height, Quaternionfc rotation) {
		double w = width / 2;
		double h = height / 2;

		Vector3dc right = rotation.transform(BASE_RIGHT, new Vector3d());
		Vector3dc up = rotation.transform(BASE_UP, new Vector3d());
		Vector3dc normal = rotation.transform(BASE_NORMAL, new Vector3d());

		Vector3dc bottomLeft = rotation.transform(new Vector3d(-w, 0, -h)).add(center);
		Vector3dc bottomRight = rotation.transform(new Vector3d(w, 0, -h)).add(center);
		Vector3dc topRight = rotation.transform(new Vector3d(w, 0, h)).add(center);
		Vector3dc topLeft = rotation.transform(new Vector3d(-w, 0, h)).add(center);

		return new Quad(center, right, up, normal, bottomLeft, bottomRight, topRight, topLeft);
	}

	public static Quad create(Plane plane, double size) {
		Quaternionf rotation = new Quaternionf();
		Vec3 normal = plane.normal();
		rotation.rotateTo(
				(float) BASE_NORMAL.x(), (float) BASE_NORMAL.y(), (float) BASE_NORMAL.z(),
				(float) normal.x, (float) normal.y, (float) normal.z
		);
		return create(TransformUtils.toJoml(plane.origin()), size, size, rotation);
	}
}
