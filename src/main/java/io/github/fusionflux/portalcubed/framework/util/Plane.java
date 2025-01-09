package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record Plane(Vector3f normal, Vector3f origin) {
	private static final Vector3f scratchPos = new Vector3f();
	private static final Vector3f scratchNormal = new Vector3f();

	public boolean test(Camera camera) {
		Vec3 camPos = camera.getPosition();
		Vector3f relativeCamPos = scratchPos.set(
				camPos.x - this.origin.x,
				camPos.y - this.origin.y,
				camPos.z - this.origin.z
		);
		return relativeCamPos.dot(this.normal) < 0;
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
