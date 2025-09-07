package io.github.fusionflux.portalcubed.framework.shape;

import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record Plane(Vec3 normal, Vec3 origin) {
	public static final StreamCodec<ByteBuf, Plane> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, Plane::normal,
			Vec3.STREAM_CODEC, Plane::origin,
			Plane::new
	);

	public Plane forward(double distance) {
		return new Plane(this.normal, this.origin.add(this.normal.scale(distance)));
	}

	public boolean isInFront(Vec3 pos) {
		Vec3 to = this.origin.vectorTo(pos);
		return to.dot(this.normal) > 0;
	}

	public boolean isBehind(Vec3 pos) {
		return !this.isInFront(pos);
	}

	@Environment(EnvType.CLIENT)
	public boolean isInFront(Camera camera) {
		return this.isInFront(camera.getPosition());
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

	@Environment(EnvType.CLIENT)
	public void getClipping(Matrix4fc view, Vec3 camPos, Vector4f dest) {
		Vec3 camRelativeOrigin = this.origin.subtract(camPos);
		Vector3f normal = view.transformDirection(this.normal.toVector3f());
		float distance = -view.transformPosition(camRelativeOrigin.toVector3f()).dot(normal);
		dest.set(normal, distance + (float) PortalRenderer.OFFSET_FROM_WALL);
	}
}
