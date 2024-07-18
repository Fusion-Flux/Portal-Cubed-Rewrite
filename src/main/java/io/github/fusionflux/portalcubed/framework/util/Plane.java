package io.github.fusionflux.portalcubed.framework.util;

import net.minecraft.client.Camera;

import net.minecraft.util.Mth;

import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public record Plane(Vector3f normal, Vector3f origin) {
	private static final Vector3f scratchPos = new Vector3f();
	private static final Vector3f scratchNormal = new Vector3f();
	private static final Matrix4f scratchMatrix = new Matrix4f();
	private static final float TEST_EPSILON = 0.09f;

	public boolean test(Camera camera) {
		Vec3 camPos = camera.getPosition();
		Vector3f relativeCamPos = scratchPos.set(
				camPos.x - this.origin.x,
				camPos.y - this.origin.y,
				camPos.z - this.origin.z
		);
		return relativeCamPos.dot(this.normal) < -TEST_EPSILON;
	}

	public void clipProjection(Matrix4f view, Vec3 camPos, Matrix4f projection) {
		Vector3f camRelativeOrigin = scratchPos.set(
				this.origin.x - camPos.x,
				this.origin.y - camPos.y,
				this.origin.z - camPos.z
		);
		int facing = Mth.sign(this.normal.dot(scratchPos));

		Vector3f normal = view.transformDirection(this.normal, scratchNormal).mul(facing);
		float distance = -view.transformPosition(camRelativeOrigin).dot(normal);

		// TODO: Better variables below this line
		Vector4f plane = new Vector4f(
				normal.x,
				normal.y,
				normal.z,
				distance
		);

		Vector4f q = projection.invertPerspective(scratchMatrix).transform(new Vector4f(
				Mth.sign(plane.x),
				Mth.sign(plane.y),
				1f,
				1f
		));
		Vector4f M4 = new Vector4f(projection.m03(), projection.m13(), projection.m23(), projection.m33());
		Vector4f c = plane.mul((2f * M4.dot(q)) / plane.dot(q));

		projection.m02(c.x - M4.x);
		projection.m12(c.y - M4.y);
		projection.m22(c.z - M4.z);
		projection.m32(c.w - M4.w);
	}
}
