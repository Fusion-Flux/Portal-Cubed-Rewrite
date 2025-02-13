package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record Plane(Vector3f normal, Vector3f origin) {
	public static final StreamCodec<ByteBuf, Plane> CODEC = StreamCodec.composite(
			ByteBufCodecs.VECTOR3F, Plane::normal,
			ByteBufCodecs.VECTOR3F, Plane::origin,
			Plane::new
	);

	private static final Vector3f scratchPos = new Vector3f();
	private static final Vector3f scratchNormal = new Vector3f();

	public boolean isInFront(Vec3 pos) {
		Vector3f relativeCamPos = scratchPos.set(
				pos.x - this.origin.x,
				pos.y - this.origin.y,
				pos.z - this.origin.z
		);
		return relativeCamPos.dot(this.normal) > 0;
	}

	public boolean isBehind(Vec3 pos) {
		return !this.isInFront(pos);
	}

	@Environment(EnvType.CLIENT)
	public boolean isInFront(Camera camera) {
		return this.isInFront(camera.getPosition());
	}

	public void getClipping(Matrix4f view, Vec3 camPos, Vector4f dest) {
		Vector3f camRelativeOrigin = scratchPos.set(
				this.origin.x - camPos.x,
				this.origin.y - camPos.y,
				this.origin.z - camPos.z
		);
		int facing = Mth.sign(this.normal.dot(camRelativeOrigin));

		Vector3f normal = view.transformDirection(this.normal, scratchNormal).mul(facing);
		float distance = -view.transformPosition(camRelativeOrigin).dot(normal);
		dest.set(normal, distance);
	}
}
