package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.UUID;

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
	public static final double THICKNESS = 0;
	public static final double COLLISION_BOX_SIDE_THICKNESS = 0.01;
	public static final double COLLISION_BOX_DEPTH = 4 - (2 * COLLISION_BOX_SIDE_THICKNESS);

    public final PortalData data;

	public final AABB plane; // technically a box, but really thin on 1 axis
	public final Vec3 normal;
	public final Quaternionf rotation;
	public final Quaternionf rotation180;

	// plane-like hole this portal punches in the world to allow walking through
	public final VoxelShape hole;
	// area in front of portal that an entity must be in for cross-portal collision to apply
	public final AABB entityCollisionArea;
	// area in front of portal where collision will be matched to behind the other portal
	public final AABB collisionCollectionArea;
	// area behind portal where collision is modified to match other portal
	public final AABB collisionModificationBox;

    public PortalInstance(PortalData data) {
        this.data = data;
		FrontAndTop orientation = data.orientation();
		this.normal = Vec3.atLowerCornerOf(orientation.front().getNormal());

		this.rotation = TransformUtils.quaternionOf(orientation);
		this.rotation180 = TransformUtils.rotateAround(rotation, orientation.top().getAxis(), 180);

		Direction.Axis frontAxis = orientation.front().getAxis();
		Direction.Axis verticalAxis = orientation.top().getAxis();
		double y = frontAxis.isVertical() ? THICKNESS : HEIGHT;
		double x = frontAxis == Direction.Axis.X ? THICKNESS : (verticalAxis == Direction.Axis.X ? HEIGHT : WIDTH);
		double z = frontAxis == Direction.Axis.Z ? THICKNESS : (verticalAxis == Direction.Axis.Z ? HEIGHT : WIDTH);
		Vec3 origin = data.origin();
		this.plane = AABB.ofSize(origin, x, y, z);

		// make hole slightly smaller than TP plane
		AABB holePlane = this.plane.inflate(-0.01);
		this.hole = Shapes.create(holePlane);

		this.entityCollisionArea = AABB.ofSize(
				origin.add(normal.scale(1.5)),  // center 1.5 blocks away
				3, 3, 3 // 3x3x3 box
		);

		this.collisionCollectionArea = this.plane.expandTowards(normal.scale(COLLISION_BOX_DEPTH)) // extend forwards
				.inflate(COLLISION_BOX_SIDE_THICKNESS) // expand bounds by thickness
				.move(normal.scale(COLLISION_BOX_SIDE_THICKNESS)); // move bounds off supporting wall

		this.collisionModificationBox = this.plane.expandTowards(normal.scale(-COLLISION_BOX_DEPTH)) // extend backwards
				.inflate(COLLISION_BOX_SIDE_THICKNESS) // expand bounds by thickness
				.move(normal.scale(-COLLISION_BOX_SIDE_THICKNESS)); // move bounds fully into supporting wall
    }

	public boolean isActive() {
		return this.linked != null;
	}

	public Vector3d relativize(Vector3d pos) {
		Vec3 origin = this.data.origin();
		return pos.sub(origin.x, origin.y, origin.z);
	}

	public Vector3d derelativize(Vector3d pos) {
		Vec3 origin = this.data.origin();
		return pos.add(origin.x, origin.y, origin.z);
	}

	public CompoundTag toNbt() {
		CompoundTag nbt = new CompoundTag();

	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeVarInt(netId);
		PacketUtils.writeVec3(buf, origin);
		buf.writeEnum(orientation);
		buf.writeEnum(shape);
		buf.writeEnum(type);
		buf.writeVarInt(color);
		buf.writeUUID(owner);
	}

	public static PortalInstance fromNetwork(FriendlyByteBuf buf) {
		int netId = buf.readVarInt();
		Vec3 origin = PacketUtils.readVec3(buf);
		FrontAndTop orientation = buf.readEnum(FrontAndTop.class);
		PortalShape shape = buf.readEnum(PortalShape.class);
		PortalType type = buf.readEnum(PortalType.class);
		int color = buf.readVarInt();
		UUID owner = buf.readUUID();
		return new PortalInstance(netId, origin, orientation, shape, type, color, owner);
	}
}
