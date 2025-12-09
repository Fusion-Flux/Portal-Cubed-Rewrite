package io.github.fusionflux.portalcubed.content.portal;

import java.util.List;

import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.Plane;
import io.github.fusionflux.portalcubed.framework.shape.Quad;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A portal in the world, with all expensive data computed.
 * There will only ever be one instance per data.
 */
public final class Portal {
	public static final Codec<Portal> CODEC = PortalData.CODEC.xmap(Portal::new, instance -> instance.data);
	public static final StreamCodec<RegistryFriendlyByteBuf, Portal> STREAM_CODEC = PortalData.STREAM_CODEC.map(Portal::new, instance -> instance.data);

	// floats strike again
	public static final double HEIGHT = 2 - 1e-3;
	public static final double WIDTH = 1 - 1e-3;

	public static final double PERIMETER_BOX_DEPTH = 1 / 16d;

    public final PortalData data;

	public final Vec3 normal;
	public final Vec3 up;
	public final Vec3 right;

	public final Quaternionf rotation180;
	public final Plane plane;

	public final Quad quad;
	public final AABB renderBounds;

	public final OBB entityCollisionArea;
	public final List<OBB> perimeterBoxes;

    public Portal(PortalData data) {
        this.data = data;
		Vec3 origin = data.origin();

		this.normal = Vec3Ext.of(this.rotation().transform(Quad.BASE_NORMAL, new Vector3d()));
		this.up = Vec3Ext.of(this.rotation().transform(Quad.BASE_UP, new Vector3d()));
		this.right = Vec3Ext.of(this.rotation().transform(Quad.BASE_RIGHT, new Vector3d()));

		this.rotation180 = SinglePortalTransform.rotate180(this.rotation());
		this.plane = new Plane(this.normal, origin);

		this.quad = Quad.create(origin.asJoml(), WIDTH, HEIGHT, this.rotation());
		this.renderBounds = this.quad.containingBox();

		Matrix3d rotationAsMatrix = new Matrix3d().rotation(this.rotation());

		Vec3 boxOrigin = origin.add(this.normal.scale(-PERIMETER_BOX_DEPTH / 2));
		// slight bonus offset (0.01) so you can walk into floor-aligned portals with no step height
		Vec3 upToBox = this.up.scale((HEIGHT / 2) + 0.51);
		Vec3 rightToBox = this.right.scale((WIDTH / 2) + 0.51);

		this.entityCollisionArea = OBB.extrudeQuad(this.quad, 128);
		// no perimeter collision when not validated, to avoid ghost collision around floating portals
		this.perimeterBoxes = !this.data.isValidated() ? List.of() : List.of(
				// top and bottom, wide
				new OBB(boxOrigin.add(upToBox), 3, PERIMETER_BOX_DEPTH, 1, rotationAsMatrix),
				new OBB(boxOrigin.add(upToBox.reverse()), 3, PERIMETER_BOX_DEPTH, 1, rotationAsMatrix),
				// right and left, tall
				new OBB(boxOrigin.add(rightToBox), 1, PERIMETER_BOX_DEPTH, 2, rotationAsMatrix),
				new OBB(boxOrigin.add(rightToBox.reverse()), 1, PERIMETER_BOX_DEPTH, 2, rotationAsMatrix)
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

	/**
	 * @return true if this portal hides the given box from collision checks
	 */
	public boolean hides(AABB box) {
		return this.plane.isPartiallyBehind(box);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Portal that && this.data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return this.data.hashCode();
	}
}
