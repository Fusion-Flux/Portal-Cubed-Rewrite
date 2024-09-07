package io.github.fusionflux.portalcubed.framework.shape;

import java.util.stream.Stream;

import io.github.fusionflux.portalcubed.framework.util.Quad;

import io.github.fusionflux.portalcubed.framework.util.TransformUtils;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Oriented Bounding Box.
 * A rotation of (0, 0, 0, 1) represents a normal towards east (+X), where relative right is south (+Z) and relative up is... up (+Y).
 */
public final class OBB {
	public static final Vec3 XP = new Vec3(1, 0, 0);
	public static final Vec3 YP = new Vec3(0, 1, 0);
	public static final Vec3 ZP = new Vec3(0, 0, 1);

	public static final boolean[] TRUE_FALSE = { true, false };

	// rotated box representation
	public final AABB bounds;
	public final Quaternionf rotation;
	// basis vectors representation
	public final Vec3 basisX;
	public final Vec3 basisY;
	public final Vec3 basisZ;
	// calculated useful values
	public final Vec3 center;
	public final AABB encompassingAabb;

	private final Vec3[] vertices;

	public OBB(AABB aabb) {
		this(aabb, new Quaternionf());
	}

	public OBB(AABB bounds, Quaternionf rotation) {
		this.bounds = bounds;
		this.rotation = rotation;

		this.basisX = TransformUtils.apply(XP, rotation::transform);
		this.basisY = TransformUtils.apply(YP, rotation::transform);
		this.basisZ = TransformUtils.apply(ZP, rotation::transform);

		this.center = bounds.getCenter();
		Vec3 lowVertex = TransformUtils.apply(new Vec3(bounds.minX, bounds.minY, bounds.minZ), rotation::transform);

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
	}

	public boolean contains(Vec3 pos) {
		return this.contains(pos.x, pos.y, pos.z);
	}

	public boolean contains(Vector3f pos) {
		return this.contains(pos.x, pos.y, pos.z);
	}

	public boolean contains(double x, double y, double z) {
		// surely the JIT will inline this, right :clueless:
		Vector3d vec = new Vector3d();
		this.rotation.transformInverse(x, y, z, vec);
		return this.bounds.contains(vec.x, vec.y, vec.z);
	}

	public boolean intersects(BlockPos pos) {
		return this.intersects(new AABB(pos));
	}

	public boolean intersects(AABB aabb) {
		return this.intersects(new OBB(aabb));
	}

	public boolean intersects(OBB other) {

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
		Vec3 offsetToCenter = normal.scale(depth / 2);
		Vec3 center = quadCenter.add(offsetToCenter);

		Quaternionf rotation = new Quaternionf();
		rotation.rotateTo()
		return new OBB(center, quad.width(), quad.height(), depth, rotation);
	}

	private Vec3 calculateVertex(Vec3 lowVertex, boolean highX, boolean highY, boolean highZ) {
		Vec3 vec = lowVertex;

		if (highX) {
			vec = vec.add(this.basisX.scale(this.bounds.getXsize() * 2));
		}
		if (highY) {
			vec = vec.add(this.basisY.scale(this.bounds.getYsize() * 2));
		}
		if (highZ) {
			vec = vec.add(this.basisZ.scale(this.bounds.getZsize() * 2));
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
