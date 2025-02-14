package io.github.fusionflux.portalcubed.content.portal;

import org.jetbrains.annotations.Unmodifiable;
import org.joml.Quaternionf;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import io.github.fusionflux.portalcubed.framework.util.Plane;
import io.github.fusionflux.portalcubed.framework.util.Quad;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A portal in the world, with all expensive data computed.
 * There will only ever be one instance per data.
 */
public final class PortalInstance {
	public static final Codec<PortalInstance> CODEC = PortalData.CODEC.xmap(PortalInstance::new, instance -> instance.data);
	public static final StreamCodec<RegistryFriendlyByteBuf, PortalInstance> STREAM_CODEC = PortalData.STREAM_CODEC.map(PortalInstance::new, instance -> instance.data);

	public static final double HEIGHT = 2;
	public static final double WIDTH = 1;

	// vanilla defines UP as having a rotation of a default quaternion
	public static final Vec3 BASE_NORMAL = new Vec3(0, 1, 0);

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

		this.normal = TransformUtils.apply(BASE_NORMAL, this.rotation()::transform);
		this.rotation180 = PortalTransform.rotate180(this.rotation());

		this.plane = new Plane(this.normal, this.data.origin());

		this.quad = Quad.create(this.rotation(), data.origin(), WIDTH, HEIGHT);
		this.renderBounds = this.quad.containingBox();

		this.entityCollisionBounds = OBB.extrudeQuad(this.quad, Integer.MAX_VALUE);
		this.blockModificationArea = OBB.extrudeQuad(this.quad, -3);
		this.blockModificationShapes = VoxelShenanigans.approximateObb(this.blockModificationArea);
    }

	public Quaternionf rotation() {
		return this.data.rotation();
	}
}
