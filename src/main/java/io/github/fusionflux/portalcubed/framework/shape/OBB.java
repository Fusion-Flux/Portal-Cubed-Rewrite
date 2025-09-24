package io.github.fusionflux.portalcubed.framework.shape;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import com.google.common.collect.Iterables;

import io.github.fusionflux.portalcubed.framework.util.SimpleIterator;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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
	// based on Entity.collideWithShapes
	private static final Direction.Axis[] collisionAxisOrder = { Direction.Axis.Y, Direction.Axis.Z, Direction.Axis.X };
	private static final Map<Direction.Axis, Vector3dc> axisVectors = Util.makeEnumMap(Direction.Axis.class, axis -> switch (axis) {
		case X -> XP;
		case Y -> YP;
		case Z -> ZP;
	});

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
		this(TransformUtils.toJoml(aabb.getCenter()), aabb.getXsize(), aabb.getYsize(), aabb.getZsize(), rotation);
	}

	public OBB(Vec3 center, double xSize, double ySize, double zSize, Matrix3dc rotation) {
		this(TransformUtils.toJoml(center), xSize, ySize, zSize, rotation);
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

	/**
	 * Hand-rolled SAT impl to find the offset needed to separate the given box from this one.
	 * @return the offset, or null if there's no collision
	 */
	@Nullable
	public Vector3d collide(AABB aabb) {
		return Sat3d.run(this.vertices(), TransformUtils.vertices(aabb), SimpleIterator.create(i -> switch (i) {
			case 0 -> XP;
			case 1 -> YP;
			case 2 -> ZP;
			case 3 -> this.basisX;
			case 4 -> this.basisY;
			case 6 -> this.basisZ;
			case 7 -> new Vector3d(XP).cross(this.basisX);
			case 8 -> new Vector3d(XP).cross(this.basisY);
			case 9 -> new Vector3d(XP).cross(this.basisZ);
			case 10 -> new Vector3d(YP).cross(this.basisX);
			case 11 -> new Vector3d(YP).cross(this.basisY);
			case 12 -> new Vector3d(YP).cross(this.basisZ);
			case 13 -> new Vector3d(ZP).cross(this.basisX);
			case 14 -> new Vector3d(ZP).cross(this.basisY);
			case 15 -> new Vector3d(ZP).cross(this.basisZ);
			default -> null;
		}));
	}

	/**
	 * Collide an AABB moving along a vector with this OBB, possibly sliding along it if a collision occurs.
	 * @param motion the motion vector. Will be modified based on collisions.
	 */
	public void collide(AABB bounds, Vector3d motion) {
		// check for an initial collision
		if (this.intersects(bounds.deflate(1e-5))) {
			// already inside the collision, do nothing in this case.
			return;
		}

		// first, do a simple collision in world coordinates to determine which face, if any, will be hit
		Vector3d motionCopy = new Vector3d(motion);
		Vector3d offsetNormal = this.collidePhaseOne(bounds, motionCopy);
		if (offsetNormal == null) {
			// no collision
			return;
		}

		// we need to determine behavior based on the normal of the hit face.
		// when the normal is approximately horizontal, re-collide, but slide this time
		if (Math.abs(offsetNormal.dot(YP)) > 1e-2) {
			// normal is not approximately horizontal.
			// use the motion vector that was just calculated.
			motion.set(motionCopy);
			return;
		}

		// when the hit face is approximately horizontal, we need to re-collide with sliding.
		// this is done so you can slide along walls, but jumping in place on a slope doesn't push you down it.

		// motion as local coords corresponds to numbers of basis vectors
		Vector3d bases = this.rotation.transform(motion, new Vector3d());

		for (Direction.Axis axis : collisionAxisOrder) {
			Vector3dc basis = this.basis(axis);
			double target = projectionLength(motion, basis);
			Result result = this.collideOnAxis(bounds, basis, target);
			double actual = result == null ? target : result.distance;
			set(bases, axis, actual);
			// update the bounding box so the next axis step starts after this one
			if (actual != 0) {
				bounds = bounds.move(basis.x() * actual, basis.y() * actual, basis.z() * actual);
			}
		}

		// preserve the y component as-is so you can't jump on near-vertical walls
		double y = motion.y;

		// recombine into a final motion vector
		motion.set(this.basisX.x() * bases.x, y, this.basisX.z() * bases.x);
		motion.add(this.basisY.x() * bases.y, 0, this.basisY.z() * bases.y);
		motion.add(this.basisZ.x() * bases.z, 0, this.basisZ.z() * bases.z);
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
		return TransformUtils.vertices(this.localAabb);
	}

	@Nullable
	private Result collideOnAxis(AABB box, Vector3dc axis, double motion) {
		if (motion == 0)
			return null;

		// garbage brute-force linear scan to find the time of impact.
		// I want to replace this with something better, but I've been researching for weeks with no results.

		final int steps = 20;

		// skip 0, already checked
		for (int step = 1; step <= steps; step++) {
			double progress = step / (double) steps;
			double offset = Mth.lerp(progress, 0, motion);
			AABB moved = box.move(axis.x() * offset, axis.y() * offset, axis.z() * offset);

			Vector3d vec = this.collide(moved);
			if (vec != null) {
				// step back 1
				double prevProgress = (step - 1) / (double) steps;
				double finalMotion = Mth.lerp(prevProgress, 0, motion);
				return new Result(finalMotion, vec);
			}
		}

		// no collision found.
		return null;
	}

	/**
	 * Collides along the 3 world-space axes to figure out which face will be hit.
	 * @return a normal vector representing the hit "face", or null if no collision occurred
	 */
	@Nullable
	private Vector3d collidePhaseOne(AABB bounds, Vector3d motion) {
		Vector3d normal = null;

		for (Direction.Axis axis : collisionAxisOrder) {
			double target = get(motion, axis);
			Vector3dc axisVec = axisVectors.get(axis);
			Result result = this.collideOnAxis(bounds, axisVec, target);
			double actual = result == null ? target : result.distance;

			if (actual != 0) {
				bounds = bounds.move(axisVec.x() * actual, axisVec.y() * actual, axisVec.z() * actual);
			}

			if (target == actual)
				continue;

			// collision occurred
			set(motion, axis, actual);

			if (normal == null && result != null) {
				normal = result.offset.normalize();
			}
		}

		return normal;
	}

	public static OBB extrudeQuad(Quad quad, double depth) {
		Vector3dc normal = quad.normal();
		Vector3dc center = normal.mul(depth / 2, new Vector3d()).add(quad.center());
		Matrix3d rotation = new Matrix3d(quad.right(), quad.up(), normal).normal();
		return new OBB(center, quad.width(), quad.height(), Math.abs(depth), rotation);
	}

	private static double projectionLength(Vector3dc a, Vector3dc b) {
		return a.dot(b) / b.lengthSquared();
	}

	private static double get(Vector3dc vec, Direction.Axis axis) {
		return switch (axis) {
			case X -> vec.x();
			case Y -> vec.y();
			case Z -> vec.z();
		};
	}

	private static void set(Vector3d vec, Direction.Axis axis, double value) {
		switch (axis) {
			case X -> vec.x = value;
			case Y -> vec.y = value;
			case Z -> vec.z = value;
		}
	}

	private record Result(double distance, Vector3d offset) {
	}
}
