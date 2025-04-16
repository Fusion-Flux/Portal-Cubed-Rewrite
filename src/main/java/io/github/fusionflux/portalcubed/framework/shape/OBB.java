package io.github.fusionflux.portalcubed.framework.shape;

import java.util.function.Function;

import org.joml.Intersectiond;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import com.google.common.collect.Iterables;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Oriented Bounding Box.
 * Identity rotation represents a normal towards east (+X), where relative right is south (+Z) and relative up is... up (+Y).
 */
public final class OBB {
	public final Vector3dc extents;
	public final Vector3dc center;
	public final Matrix3dc rotation;
	public final Matrix3dc inverseRotation;

	public final AABB encompassingAabb;

	public OBB(Vector3dc center, double xSize, double ySize, double zSize, Matrix3dc rotation) {
		this.extents = new Vector3d(xSize / 2, ySize / 2, zSize / 2);
		this.center = center;
		this.rotation = rotation;
		this.inverseRotation = rotation.invert(new Matrix3d());

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
		return this.intersects(new AABB(pos).contract(1.0E-7, 1.0E-7, 1.0E-7));
	}

	public boolean intersects(AABB aabb) {
		Vec3 aabbCenter = aabb.getCenter();
		return Intersectiond.testObOb(
				this.center.x(), this.center.y(), this.center.z(),
				this.rotation.m00(), this.rotation.m01(), this.rotation.m02(),
				this.rotation.m10(), this.rotation.m11(), this.rotation.m12(),
				this.rotation.m20(), this.rotation.m21(), this.rotation.m22(),
				this.extents.x(), this.extents.y(), this.extents.z(),
				aabbCenter.x, aabbCenter.y, aabbCenter.z,
				1, 0, 0,
				0, 1, 0,
				0, 0, 1,
				aabb.getXsize() / 2, aabb.getYsize() / 2, aabb.getZsize() / 2
		);
	}

	public boolean intersects(OBB that) {
		return Intersectiond.testObOb(
				this.center.x(), this.center.y(), this.center.z(),
				this.rotation.m00(), this.rotation.m01(), this.rotation.m02(),
				this.rotation.m10(), this.rotation.m11(), this.rotation.m12(),
				this.rotation.m20(), this.rotation.m21(), this.rotation.m22(),
				this.extents.x(), this.extents.y(), this.extents.z(),
				that.center.x(), that.center.y(), that.center.z(),
				that.rotation.m00(), that.rotation.m01(), that.rotation.m02(),
				that.rotation.m10(), that.rotation.m11(), that.rotation.m12(),
				that.rotation.m20(), that.rotation.m21(), that.rotation.m22(),
				that.extents.x(), that.extents.y(), that.extents.z()
		);
	}

	public Iterable<BlockPos> intersectingBlocks() {
		return Iterables.filter(BlockPos.betweenClosed(
				Mth.floor(this.encompassingAabb.minX),
				Mth.floor(this.encompassingAabb.minY),
				Mth.floor(this.encompassingAabb.minZ),
				Mth.floor(this.encompassingAabb.maxX),
				Mth.floor(this.encompassingAabb.maxY),
				Mth.floor(this.encompassingAabb.maxZ)
		), this::intersects);
	}

	public static OBB extrudeQuad(Quad quad, double depth) {
		Vector3dc up = quad.up();
		Vector3dc normal = quad.normal();
		Vector3dc right = up.cross(normal, new Vector3d()).normalize();

		Matrix3d rotation = new Matrix3d(
				right.x(), right.y(), right.z(),
				up.x(), up.y(), up.z(),
				normal.x(), normal.y(), normal.z()
		);

		Vector3dc center = normal.mul(depth / 2, new Vector3d()).add(quad.center());
		return new OBB(center, quad.width(), quad.height(), Math.abs(depth), rotation);
	}
}
