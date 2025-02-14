package io.github.fusionflux.portalcubed.content.portal;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PortalTransform {
	public static final StreamCodec<ByteBuf, PortalTransform> CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC, transform -> transform.inOrigin,
			PortalCubedStreamCodecs.QUATERNIONFC, transform -> transform.inRot,
			Vec3.STREAM_CODEC, transform -> transform.outOrigin,
			PortalCubedStreamCodecs.QUATERNIONFC, transform -> transform.outRot,
			PortalTransform::new
	);

	public final Vec3 inOrigin;
	private final Quaternionfc inRot;
	private final Quaternionfc inRot180;

	private final Vec3 outOrigin;
	private final Quaternionfc outRot;
	private final Quaternionfc outRot180;

	public PortalTransform(PortalInstance in, PortalInstance out) {
		this(in.data.origin(), in.data.rotation(), out.data.origin(), out.data.rotation());
	}

	public PortalTransform(Vec3 inOrigin, Quaternionfc inRot, Vec3 outOrigin, Quaternionfc outRot) {
		this.inOrigin = inOrigin;
		this.inRot = new Quaternionf(inRot);
		this.inRot180 = rotate180(inRot);
		this.outOrigin = outOrigin;
		this.outRot = new Quaternionf(outRot);
		this.outRot180 = rotate180(outRot);
	}

	public PortalTransform inverse() {
		return new PortalTransform(this.outOrigin, this.outRot, this.inOrigin, this.inRot);
	}

	public Vec3 applyRelative(Vec3 pos) {
		Vector3f vec3f = pos.toVector3f();
		this.inRot.transformInverse(vec3f);
		this.outRot180.transform(vec3f);
		return new Vec3(vec3f);
	}

	public Vec3 applyAbsolute(Vec3 pos) {
		Vec3 relative = pos.subtract(this.inOrigin);
		Vec3 transformed = this.applyRelative(relative);
		return transformed.add(this.outOrigin);
	}

	public void print() {
	}

	public static Quaternionf rotate180(Quaternionfc rotation) {
		return rotation.rotateZ(Mth.DEG_TO_RAD * 180, new Quaternionf());
	}
}
