package io.github.fusionflux.portalcubed.content.portal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import io.github.fusionflux.portalcubed.framework.shape.Quad;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A portal in the world, with all expensive data computed.
 * There will only ever be one instance per data.
 */
public final class PortalInstance {
	public static final Codec<PortalInstance> CODEC = PortalData.CODEC.xmap(PortalInstance::new, instance -> instance.data);
	public static final StreamCodec<RegistryFriendlyByteBuf, PortalInstance> STREAM_CODEC = PortalData.STREAM_CODEC.map(PortalInstance::new, instance -> instance.data);

	// floats strike again
	public static final double HEIGHT = 2 - 1e-3;
	public static final double WIDTH = 1 - 1e-3;

    public final PortalData data;

	public final Vec3 normal;
	public final Vec3 up;
	public final Vec3 right;

	public final Quaternionf rotation180;
	public final Plane plane;

	public final Quad quad;
	public final AABB renderBounds;

	public final OBB entityCollisionArea;
	public final OBB blockModificationArea;
	public final List<OBB> perimeterBoxes;

    public PortalInstance(PortalData data) {
        this.data = data;
		Vec3 origin = data.origin();

		this.normal = TransformUtils.toMc(this.rotation().transform(Quad.BASE_NORMAL, new Vector3d()));
		this.up = TransformUtils.toMc(this.rotation().transform(Quad.BASE_UP, new Vector3d()));
		this.right = TransformUtils.toMc(this.rotation().transform(Quad.BASE_RIGHT, new Vector3d()));

		this.rotation180 = SinglePortalTransform.rotate180(this.rotation());
		this.plane = new Plane(this.normal, origin);

		this.quad = Quad.create(TransformUtils.toJoml(origin), WIDTH, HEIGHT, this.rotation());
		this.renderBounds = this.quad.containingBox();

		Matrix3d rotationAsMatrix = new Matrix3d().rotation(this.rotation());

		Vec3 boxOrigin = origin.add(this.normal.scale(-0.05));
		Vec3 upToBox = this.up.scale((HEIGHT / 2) + 0.5);
		Vec3 rightToBox = this.right.scale((WIDTH / 2) + 0.5);

		this.entityCollisionArea = OBB.extrudeQuad(this.quad, 128);
		this.blockModificationArea = OBB.extrudeQuad(this.quad, -128);
		// no perimeter collision when not validated, to avoid ghost collision around floating portals
		this.perimeterBoxes = !this.data.isValidated() ? List.of() : List.of(
				// top and bottom, wide
				new OBB(boxOrigin.add(upToBox), 3, 0.1, 1, rotationAsMatrix),
				new OBB(boxOrigin.add(upToBox.reverse()), 3, 0.1, 1, rotationAsMatrix),
				// right and left, tall
				new OBB(boxOrigin.add(rightToBox), 1, 0.1, 2, rotationAsMatrix),
				new OBB(boxOrigin.add(rightToBox.reverse()), 1, 0.1, 2, rotationAsMatrix)
		);
    }

	public PortalType type() {
		return this.data.type().value();
	}

	public Quaternionf rotation() {
		return this.data.rotation();
	}

	public boolean seesModifiedCollision(Entity entity) {
		return this.entityCollisionArea.intersects(entity.getBoundingBox());
	}

	public boolean modifiesCollision(BlockPos pos) {
		return this.blockModificationArea.intersects(pos);
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
