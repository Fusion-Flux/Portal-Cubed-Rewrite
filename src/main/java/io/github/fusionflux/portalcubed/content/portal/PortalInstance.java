package io.github.fusionflux.portalcubed.content.portal;

import org.jetbrains.annotations.Unmodifiable;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import io.github.fusionflux.portalcubed.framework.util.Plane;
import io.github.fusionflux.portalcubed.framework.util.Quad;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A portal in the world, with all expensive data computed.
 * There will only ever be one instance per data.
 */
public final class PortalInstance {
	public static final Codec<PortalInstance> CODEC = PortalData.CODEC.xmap(PortalInstance::new, instance -> instance.data);
	public static final StreamCodec<ByteBuf, PortalInstance> STREAM_CODEC = PortalData.STREAM_CODEC.map(PortalInstance::new, instance -> instance.data);

	public static final double HEIGHT = 2;
	public static final double WIDTH = 1;

    public final PortalData data;

	public final Vec3 normal;
	public final Quaternionf rotation180;

	public final Plane plane;

	public final Quad quad;
	public final AABB renderBounds;

	public final OBB entityCollisionBounds;
	public final OBB blockModificationArea;
	@Unmodifiable
	public final Object2ObjectMap<BlockPos, VoxelShape> blockModificationShapes;

    public PortalInstance(PortalData data) {
        this.data = data;

		this.normal = TransformUtils.apply(TransformUtils.ZP, this.rotation()::transform);
		this.rotation180 = new Quaternionf(this.rotation());
		this.rotation180.rotateY(Mth.DEG_TO_RAD * 180);

		this.plane = new Plane(this.rotation().transform(0, 0, 1, new Vector3f()), this.data.origin().toVector3f());

		this.quad = Quad.create(this.rotation(), data.origin(), WIDTH, HEIGHT);
		this.renderBounds = this.quad.containingBox();

		this.entityCollisionBounds = OBB.extrudeQuad(this.quad, Integer.MAX_VALUE);
		this.blockModificationArea = OBB.extrudeQuad(this.quad, -3);
		this.blockModificationShapes = VoxelShenanigans.approximateObb(this.blockModificationArea);
    }

	public Vector3d relativize(Vector3d pos) {
		Vec3 origin = this.data.origin();
		return pos.sub(origin.x, origin.y, origin.z);
	}

	public Vector3d derelativize(Vector3d pos) {
		Vec3 origin = this.data.origin();
		return pos.add(origin.x, origin.y, origin.z);
	}

	public Quaternionf rotation() {
		return this.data.rotation();
	}
}
