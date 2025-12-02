package io.github.fusionflux.portalcubed.framework.shape;

import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;
import org.joml.Matrix4fc;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record Plane(Vec3 normal, Vec3 origin) {
	public static final StreamCodec<ByteBuf, Plane> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, Plane::normal,
			Vec3.STREAM_CODEC, Plane::origin,
			Plane::new
	);

	public Plane(Vector3dc normal, Vector3dc origin) {
		this(Vec3Ext.of(normal), Vec3Ext.of(origin));
	}

	/**
	 * @return true if the given pos is not on or in front of this plane
	 */
	public boolean isBehind(Vector3dc pos) {
		// inlined pos.sub(this.origin)
		double dx = pos.x() - this.origin.x;
		double dy = pos.y() - this.origin.y;
		double dz = pos.z() - this.origin.z;
		// inlined dot product
		return (dx * this.normal.x) + (dy * this.normal.y) + (dz * this.normal.z) < 0;
	}

	/**
	 * @return true if the given pos is behind or on this plane
	 */
	public boolean isBehindOrOn(Vector3dc pos) {
		// inlined pos.sub(this.origin)
		double dx = pos.x() - this.origin.x;
		double dy = pos.y() - this.origin.y;
		double dz = pos.z() - this.origin.z;
		// inlined dot product
		return (dx * this.normal.x) + (dy * this.normal.y) + (dz * this.normal.z) <= 0;
	}
	/**
	 * @see #isBehind(Vector3dc)
	 */
	public boolean isBehind(Vec3 pos) {
		return this.isBehind(pos.asJoml());
	}

	/**
	 * @return true if any vertex of the box is {@link #isBehind(Vector3dc) behind} this plane
	 */
	public boolean isPartiallyBehind(AABB box) {
		for (Vector3dc vertex : box.vertices()) {
			if (this.isBehind(vertex)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return true if all vertices of the box are {@link #isBehindOrOn(Vector3dc) behind or on} this plane
	 */
	public boolean isFullyBehindOrOn(AABB box) {
		for (Vector3dc vertex : box.vertices()) {
			if (!this.isBehindOrOn(vertex)) {
				return false;
			}
		}

		return true;
	}

	@Nullable
	public Vec3 clip(Vec3 from, Vec3 to) {
		Vec3 direction = from.vectorTo(to).normalize();
		double distance = Intersectiond.intersectRayPlane(
				from.x, from.y, from.z,
				direction.x, direction.y, direction.z,
				this.origin.x, this.origin.y, this.origin.z,
				this.normal.x, this.normal.y, this.normal.z,
				1e-5
		);

		return distance == -1 ? null : from.add(direction.scale(distance));
	}

	public void getClipping(Matrix4fc view, Vec3 camPos, Vector4f dest) {
		Vec3 camRelativeOrigin = this.origin.subtract(camPos);
		Vector3f normal = view.transformDirection(this.normal.toVector3f());
		float distance = -view.transformPosition(camRelativeOrigin.toVector3f()).dot(normal);
		// this is server-safe since javac inlines primitive constants
		dest.set(normal, distance + (float) PortalRenderer.OFFSET_FROM_WALL);
	}
}
