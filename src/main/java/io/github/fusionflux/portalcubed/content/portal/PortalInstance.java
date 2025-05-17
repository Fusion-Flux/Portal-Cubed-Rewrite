package io.github.fusionflux.portalcubed.content.portal;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Unmodifiable;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import io.github.fusionflux.portalcubed.framework.shape.Quad;
import io.github.fusionflux.portalcubed.framework.shape.voxel.VoxelShenanigans;
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

	public static final double BUFFER = 1e-3;
	public static final double HEIGHT = 2 - BUFFER;
	public static final double WIDTH = 1 - BUFFER;

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

		this.normal = TransformUtils.toMc(this.rotation().transform(Quad.BASE_NORMAL, new Vector3d()));
		this.rotation180 = SinglePortalTransform.rotate180(this.rotation());

		this.plane = new Plane(this.normal, this.data.origin());

		this.quad = Quad.create(TransformUtils.toJoml(data.origin()), WIDTH, HEIGHT, this.rotation());
		this.renderBounds = this.quad.containingBox();

		this.entityCollisionBounds = OBB.extrudeQuad(this.quad, Integer.MAX_VALUE);
		this.blockModificationArea = OBB.extrudeQuad(this.quad, -3);
		this.blockModificationShapes = VoxelShenanigans.approximateObb(this.blockModificationArea);
    }

	public PortalType type() {
		return this.data.type().value();
	}

	public Quaternionf rotation() {
		return this.data.rotation();
	}

	public record Holder(PortalPair.Holder pair, Polarity polarity, PortalInstance portal) {
		public Optional<Holder> opposite() {
			return this.pair.get(this.polarity.opposite());
		}

		public PortalId asId() {
			return new PortalId(this.pair.key(), this.polarity);
		}

		public boolean matches(PortalId id) {
			return this.pair.key().equals(id.key()) && this.polarity == id.polarity();
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.pair, this.polarity);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Holder that && this.pair.equals(that.pair) && this.polarity == that.polarity;
		}
	}
}
