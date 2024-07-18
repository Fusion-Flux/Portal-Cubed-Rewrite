package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.util.Plane;
import io.github.fusionflux.portalcubed.framework.util.Quad;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * A portal in the world, with all expensive data computed.
 * There will only ever be one instance per data.
 */
public final class PortalInstance {
	public static final Codec<PortalInstance> CODEC = PortalData.CODEC.xmap(PortalInstance::new, instance -> instance.data);

	// portal plane is 2 pixels short of full blocks on each axis, 1 on each side
	public static final double SIXTEENTH = 1 / 16f;
	public static final double HEIGHT = 2 - (2 * SIXTEENTH);
	public static final double WIDTH = 1 - (2 * SIXTEENTH);

    public final PortalData data;

	public final Quad quad;
	public final AABB renderBounds;
	public final Vec3 normal;
	public final Quaternionf rotation;
	public final Quaternionf rotation180;
	public final Plane plane;

    public PortalInstance(PortalData data) {
        this.data = data;
		FrontAndTop orientation = data.orientation();
		this.normal = Vec3.atLowerCornerOf(orientation.front().getNormal());

		this.rotation = TransformUtils.quaternionOf(orientation);
		this.rotation180 = TransformUtils.rotateAround(rotation, orientation.top().getAxis(), 180);
		this.plane = new Plane(this.rotation.transform(0, 0, 1, new Vector3f()), this.data.origin().toVector3f());

		this.quad = Quad.create(this.rotation, data.origin(), WIDTH, HEIGHT);
		this.renderBounds = this.quad.containingBox();
    }

	public Vector3d relativize(Vector3d pos) {
		Vec3 origin = this.data.origin();
		return pos.sub(origin.x, origin.y, origin.z);
	}

	public Vector3d derelativize(Vector3d pos) {
		Vec3 origin = this.data.origin();
		return pos.add(origin.x, origin.y, origin.z);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		// todo: real impl
		buf.writeJsonWithCodec(CODEC, this);
	}

	public static PortalInstance fromNetwork(FriendlyByteBuf buf) {
		return buf.readJsonWithCodec(CODEC);
	}
}
