package io.github.fusionflux.portalcubed.framework.shape;

import java.util.Objects;
import java.util.function.Function;

import org.joml.Intersectiond;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import com.google.common.collect.Iterables;

import io.github.fusionflux.portalcubed.framework.util.Maath;
import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Oriented Bounding Box.
 * Identity rotation represents a normal towards east (+X), where relative right is south (+Z) and relative up is... up (+Y).
 */
public final class OBB {
	public static final Vector3dc XP = new Vector3d(1, 0, 0);
	public static final Vector3dc YP = new Vector3d(0, 1, 0);
	public static final Vector3dc ZP = new Vector3d(0, 0, 1);
	public static final Vector3dc ZERO = new Vector3d();

	public final Vector3dc extents;
	public final Vector3dc center;
	public final Matrix3dc rotation;
	public final Matrix3dc inverseRotation;

	public final Vector3dc basisX;
	public final Vector3dc basisY;
	public final Vector3dc basisZ;

	public final AABB localAabb;
	public final AABB encompassingAabb;

	public OBB(BlockPos pos) {
		this(new AABB(pos));
	}

	public OBB(AABB aabb) {
		this(aabb, new Matrix3d());
	}

	public OBB(AABB aabb, Matrix3dc rotation) {
		this(aabb.getCenter().asJoml(), aabb.getXsize(), aabb.getYsize(), aabb.getZsize(), rotation);
	}

	public OBB(Vec3 center, double xSize, double ySize, double zSize, Matrix3dc rotation) {
		this(center.asJoml(), xSize, ySize, zSize, rotation);
	}

	public OBB(Vector3dc center, double xSize, double ySize, double zSize, Matrix3dc rotation) {
		this.extents = new Vector3d(xSize / 2, ySize / 2, zSize / 2);
		this.center = new Vector3d(center);
		this.rotation = new Matrix3d(rotation);
		this.inverseRotation = rotation.invert(new Matrix3d());

		this.basisX = rotation.transform(new Vector3d(1, 0, 0));
		this.basisY = rotation.transform(new Vector3d(0, 1, 0));
		this.basisZ = rotation.transform(new Vector3d(0, 0, 1));

		this.localAabb = new AABB(
				center.x() - this.extents.x(), center.y() - this.extents.y(), center.z() - this.extents.z(),
				center.x() + this.extents.x(), center.y() + this.extents.y(), center.z() + this.extents.z()
		);

		double highX = Math.abs(rotation.m00()) * this.extents.x() + (Math.abs(rotation.m10()) * this.extents.y() + (Math.abs(rotation.m20()) * this.extents.z()));
		double highY = Math.abs(rotation.m01()) * this.extents.x() + (Math.abs(rotation.m11()) * this.extents.y() + (Math.abs(rotation.m21()) * this.extents.z()));
		double highZ = Math.abs(rotation.m02()) * this.extents.x() + (Math.abs(rotation.m12()) * this.extents.y() + (Math.abs(rotation.m22()) * this.extents.z()));
		this.encompassingAabb = new AABB(
				center.x() - highX,
				center.y() - highY,
				center.z() - highZ,
				center.x() + highX,
				center.y() + highY,
				center.z() + highZ
		);
	}

	public Vector3dc basis(Direction.Axis axis) {
		return switch (axis) {
			case X -> this.basisX;
			case Y -> this.basisY;
			case Z -> this.basisZ;
		};
	}

	public Vector3dc normal(Direction direction) {
		Vector3dc basis = this.basis(direction.getAxis());
		if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE)
			return basis;

		return new Vector3d(basis).mul(-1);
	}

	public double extent(Direction.Axis axis) {
		return Maath.get(this.extents, axis);
	}

	public Plane plane(Direction direction) {
		Vector3dc normal = this.normal(direction);
		double extent = this.extent(direction.getAxis());
		Vector3dc origin = new Vector3d(normal).mul(extent).add(this.center);

		return new Plane(normal, origin);
	}

	public OBB transformed(Function<Vector3d, Vector3dc> center, Function<Matrix3d, Matrix3dc> rotation) {
		return new OBB(
				center.apply(new Vector3d(this.center)),
				this.extents.x() * 2, this.extents.y() * 2, this.extents.z() * 2,
				rotation.apply(new Matrix3d(this.rotation))
		);
	}

	public boolean contains(Vec3 pos) {
		return this.contains(pos.x, pos.y, pos.z);
	}

	public boolean contains(Vector3f pos) {
		return this.contains(pos.x, pos.y, pos.z);
	}

	// This function is specially designed for performance due to VoxelShape approximation, please forgive my coding sins.
	public boolean contains(double x, double y, double z) {
		double relativeX = x - this.center.x();
		double relativeY = y - this.center.y();
		double relativeZ = z - this.center.z();
		double transformedX = this.inverseRotation.m00() * relativeX + this.inverseRotation.m10() * relativeY + this.inverseRotation.m20() * relativeZ;
		double transformedY = this.inverseRotation.m01() * relativeX + this.inverseRotation.m11() * relativeY + this.inverseRotation.m21() * relativeZ;
		double transformedZ = this.inverseRotation.m02() * relativeX + this.inverseRotation.m12() * relativeY + this.inverseRotation.m22() * relativeZ;
		return (transformedX >= -this.extents.x()) && (transformedX <= this.extents.x()) && (transformedY >= -this.extents.y()) && (transformedY <= this.extents.y()) && (transformedZ >= -this.extents.z()) && (transformedZ <= this.extents.z());
	}

	public boolean intersects(BlockPos pos) {
		return this.intersects(new AABB(pos).deflate(1e-7));
	}

	public boolean intersects(AABB aabb) {
		return this.collide(aabb, ZERO) == DynamicSat3d.COLLIDING;
	}

	public boolean intersectsSphere(Vec3 center, double radius) {
		Vector3d transformedCenter = new Vector3d(center.asJoml()).sub(this.center);
		this.inverseRotation.transform(transformedCenter);
		transformedCenter.add(this.center);

		AABB aabb = this.localAabb;
		return Intersectiond.testAabSphere(
				aabb.minX, aabb.minY, aabb.minZ,
				aabb.maxX, aabb.maxY, aabb.maxZ,
				transformedCenter.x, transformedCenter.y, transformedCenter.z,
				radius * radius
		);
	}

	/**
	 * Calculate how far the given box can move along the given axis before a collision occurs, up to and including {@code motion}.
	 */
	public double collide(AABB bounds, Direction.Axis axis, double motion) {
		Vector3dc axisVector = switch (axis) {
			case X -> XP;
			case Y -> YP;
			case Z -> ZP;
		};

		return this.collideOnAxis(bounds, axisVector, motion);
	}

	public Iterable<BlockPos> intersectingBlocks() {
		return Iterables.filter(BlockPos.betweenClosed(this.encompassingAabb), this::intersects);
	}

	public Iterable<Vector3dc> vertices() {
		return Iterables.transform(this.localVertices(), vertex -> {
			Objects.requireNonNull(vertex); // shut
			Vector3d copy = new Vector3d(vertex);
			copy.sub(this.center);
			this.rotation.transform(copy);
			copy.add(this.center);
			return copy;
		});
	}

	public Iterable<Vector3dc> localVertices() {
		return this.localAabb.vertices();
	}

	private double collideOnAxis(AABB box, Vector3dc axis, double motion) {
		Vector3d motionVector = new Vector3d(axis).mul(motion);
		double scale = this.collide(box, motionVector);
		return scale == DynamicSat3d.COLLIDING ? motion : motion * scale;
	}

	private double collide(AABB aabb, Vector3dc motion) {
		return DynamicSat3d.run(this.vertices(), aabb.vertices(), motion, SimpleIterator.create(i -> switch (i) {
			case 0  -> XP;
			case 1  -> YP;
			case 2  -> ZP;
			case 3  -> this.basisX;
			case 4  -> this.basisY;
			case 5  -> this.basisZ;
			case 6  -> new Vector3d(XP).cross(this.basisX);
			case 7  -> new Vector3d(XP).cross(this.basisY);
			case 8  -> new Vector3d(XP).cross(this.basisZ);
			case 9  -> new Vector3d(YP).cross(this.basisX);
			case 10 -> new Vector3d(YP).cross(this.basisY);
			case 11 -> new Vector3d(YP).cross(this.basisZ);
			case 12 -> new Vector3d(ZP).cross(this.basisX);
			case 13 -> new Vector3d(ZP).cross(this.basisY);
			case 14 -> new Vector3d(ZP).cross(this.basisZ);
			default -> null;
		}));
	}

	public static OBB extrudeQuad(Quad quad, double depth) {
		Vector3dc normal = quad.normal();
		Vector3dc center = normal.mul(depth / 2, new Vector3d()).add(quad.center());
		Matrix3d rotation = new Matrix3d(quad.right(), quad.up(), normal).normal();
		return new OBB(center, quad.width(), quad.height(), Math.abs(depth), rotation);
	}
}
