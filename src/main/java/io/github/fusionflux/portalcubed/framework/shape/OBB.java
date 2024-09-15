package io.github.fusionflux.portalcubed.framework.shape;

import java.util.stream.Stream;

import io.github.fusionflux.portalcubed.framework.util.Line;
import io.github.fusionflux.portalcubed.framework.util.Quad;

import io.github.fusionflux.portalcubed.framework.util.TransformUtils;

import org.joml.Intersectiond;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Oriented Bounding Box.
 * A rotation of (0, 0, 0, 1) represents a normal towards east (+X), where relative right is south (+Z) and relative up is... up (+Y).
 */
public final class OBB {
	public static final boolean[] TRUE_FALSE = { true, false };

	public final double xSize;
	public final double ySize;
	public final double zSize;
	public final Vec3 center;
	public final Quaternionf rotation;
	// basis vectors / normals
	public final Vec3 basisX;
	public final Vec3 basisY;
	public final Vec3 basisZ;
	// calculated useful values
	public final AABB encompassingAabb;
	public final Vec3[] vertices;
	public final Line[] edges;

	private final AABB relativeBounds;

	public OBB(Vec3 center, double xSize, double ySize, double zSize, Quaternionf rotation) {
		this(AABB.ofSize(center, xSize, ySize, zSize), rotation);
	}

	public OBB(AABB aabb) {
		this(aabb, new Quaternionf());
	}

	public OBB(AABB bounds, Quaternionf rotation) {
		this.xSize = bounds.getXsize();
		this.ySize = bounds.getYsize();
		this.zSize = bounds.getZsize();
		this.center = bounds.getCenter();

		// move bounds to origin for calculations
		this.relativeBounds = bounds.move(-this.center.x, -this.center.y, -this.center.z);

		this.rotation = rotation;

		this.basisX = TransformUtils.apply(TransformUtils.XP, rotation::transform);
		this.basisY = TransformUtils.apply(TransformUtils.YP, rotation::transform);
		this.basisZ = TransformUtils.apply(TransformUtils.ZP, rotation::transform);

		Vec3 lowVertex = TransformUtils.apply(
				new Vec3(this.relativeBounds.minX, this.relativeBounds.minY, this.relativeBounds.minZ),
				rotation::transform,
				vec -> vec.add(this.center.x, this.center.y, this.center.z)
		);

		// calculate vertices and encompassing AABB

		this.vertices = new Vec3[8];
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		double minZ = Integer.MAX_VALUE;
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;
		double maxZ = Integer.MIN_VALUE;

		for (boolean highX : TRUE_FALSE) {
			for (boolean highY : TRUE_FALSE) {
				for (boolean highZ : TRUE_FALSE) {
					Vec3 vertex = this.calculateVertex(lowVertex, highX, highY, highZ);
					int key = vertexKey(highX, highY, highZ);
					this.vertices[key] = vertex;

					minX = Math.min(minX, vertex.x);
					minY = Math.min(minY, vertex.y);
					minZ = Math.min(minZ, vertex.z);
					maxX = Math.max(maxX, vertex.x);
					maxY = Math.max(maxY, vertex.y);
					maxZ = Math.max(maxZ, vertex.z);
				}
			}
		}

		this.encompassingAabb = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

		this.edges = new Line[] {
				// top 4
				new Line(this.vertex(false, true, false), this.vertex(true, true, false)),
				new Line(this.vertex(false, true, false), this.vertex(false, true, true)),
				new Line(this.vertex(true, true, true), this.vertex(true, true, false)),
				new Line(this.vertex(true, true, true), this.vertex(false, true, true)),
				// bottom 4
				new Line(this.vertex(false, false, false), this.vertex(true, false, false)),
				new Line(this.vertex(false, false, false), this.vertex(false, false, true)),
				new Line(this.vertex(true, false, true), this.vertex(true, false, false)),
				new Line(this.vertex(true, false, true), this.vertex(false, false, true)),
				// 4 vertical connections
				new Line(this.vertex(false, false, false), this.vertex(false, true, false)),
				new Line(this.vertex(true, false, false), this.vertex(true, true, false)),
				new Line(this.vertex(false, false, true), this.vertex(false, true, true)),
				new Line(this.vertex(true, false, true), this.vertex(true, true, true)),
		};
	}

	public boolean contains(Vec3 pos) {
		return this.contains(pos.x, pos.y, pos.z);
	}

	public boolean contains(Vector3f pos) {
		return this.contains(pos.x, pos.y, pos.z);
	}

	public boolean contains(double x, double y, double z) {
		// surely the JIT will inline this, right :clueless:
		Vector3d vec = new Vector3d(x, y, z);
		vec.sub(this.center.x, this.center.y, this.center.z);
		this.rotation.transformInverse(vec);
		return this.relativeBounds.contains(vec.x, vec.y, vec.z);
	}

	public boolean intersects(BlockPos pos) {
		return this.intersects(new AABB(pos));
	}

	public boolean intersects(AABB aabb) {
		return this.intersects(new OBB(aabb));
	}

	public boolean intersects(OBB that) {
		return Intersectiond.testObOb(
				this.center.x, this.center.y, this.center.z,
				this.basisX.x, this.basisX.y, this.basisX.z,
				this.basisY.x, this.basisY.y, this.basisY.z,
				this.basisZ.x, this.basisZ.y, this.basisZ.z,
				this.xSize / 2, this.ySize / 2, this.zSize / 2,
				that.center.x, that.center.y, that.center.z,
				that.basisX.x, that.basisX.y, that.basisX.z,
				that.basisY.x, that.basisY.y, that.basisY.z,
				that.basisZ.x, that.basisZ.y, that.basisZ.z,
				that.xSize / 2, that.ySize / 2, that.zSize / 2
		);
	}

	public Stream<BlockPos> intersectingBlocks() {
		return BlockPos.betweenClosedStream(this.encompassingAabb).filter(this::intersects);
	}

	public Vec3 vertex(boolean highX, boolean highY, boolean highZ) {
		return this.vertices[vertexKey(highX, highY, highZ)];
	}

	public static OBB extrudeQuad(Quad quad, double depth) {
		Vec3 quadCenter = quad.center();
		Vec3 normal = quad.normal();
		Vec3 up = quad.up();
		Vec3 offsetToCenter = normal.scale(depth / 2);
		Vec3 center = quadCenter.add(offsetToCenter);

		Quaternionf rotation = new Quaternionf();
		Vec3 t = up.cross(normal).normalize();
		Vec3 h = normal.cross(t);
		rotation.setFromNormalized(new Matrix3d(
				t.x, t.y, t.z,
				h.x, h.y, h.z,
				normal.x, normal.y, normal.z
		));
		rotation.rotateY(Mth.DEG_TO_RAD * 90);
		return new OBB(center, depth, quad.height(), quad.width(), rotation);
	}

	private Vec3 calculateVertex(Vec3 lowVertex, boolean highX, boolean highY, boolean highZ) {
		Vec3 vec = lowVertex;

		if (highX) {
			vec = vec.add(this.basisX.scale(this.xSize));
		}
		if (highY) {
			vec = vec.add(this.basisY.scale(this.ySize));
		}
		if (highZ) {
			vec = vec.add(this.basisZ.scale(this.zSize));
		}

		return vec;
	}

	private static int vertexKey(boolean highX, boolean highY, boolean highZ) {
		int key = 0;

		if (highX) {
			key |= 0b100;
		}
		if (highY) {
			key |= 0b010;
		}
		if (highZ) {
			key |= 0b001;
		}

		return key;
	}

}
