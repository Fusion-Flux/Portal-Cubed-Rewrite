package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record Plane(Vec3 normal, Vec3 origin) {
	public static final StreamCodec<ByteBuf, Plane> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, Plane::normal,
			Vec3.STREAM_CODEC, Plane::origin,
			Plane::new
	);

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

	@Environment(EnvType.CLIENT)
	public void getClipping(Matrix4fc view, Vec3 camPos, Vector4f dest) {
		Vec3 camRelativeOrigin = this.origin.subtract(camPos);
		int facing = Mth.sign(this.normal.dot(camRelativeOrigin));

		Vector3f normal = view.transformDirection(this.normal.toVector3f()).mul(facing);
		float distance = -view.transformPosition(camRelativeOrigin.toVector3f()).dot(normal);
		dest.set(normal, distance);
	}
}
