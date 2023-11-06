package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Portal {
	// portal plane is 2 pixels short of full blocks on each axis, 1 on each side
	public static final double SIXTEENTH = 1 / 16f;
	public static final double HEIGHT = 2 - (2 * SIXTEENTH);
	public static final double WIDTH = 1 - (2 * SIXTEENTH);
	public static final double THICKNESS = 0.001;
	public static final double HOLE_DEPTH = 5;

	public final int netId;
    public final Vec3 origin;
	public final AABB plane; // technically a box, but really thin on 1 axis
	public final AABB holeBox;
	public final VoxelShape hole; // the hole this portal punches in the world to allow walking through
	public final Vec3 normal;
	public final FrontAndTop orientation;
    public final PortalShape shape;
    public final PortalType type;
	public final int color;

    public Portal(int netId, Vec3 origin, FrontAndTop orientation, PortalShape shape, PortalType type, int color) {
		this.netId = netId;
        this.origin = origin;
		this.normal = Vec3.atLowerCornerOf(orientation.front().getNormal());
		this.orientation = orientation;
        this.shape = shape;
        this.type = type;
		this.color = color;

		Direction.Axis frontAxis = orientation.front().getAxis();
		Direction.Axis verticalAxis = orientation.top().getAxis();
		double y = frontAxis.isVertical() ? THICKNESS : HEIGHT;
		double x = frontAxis == Direction.Axis.X ? THICKNESS : (verticalAxis == Direction.Axis.X ? HEIGHT : WIDTH);
		double z = frontAxis == Direction.Axis.Z ? THICKNESS : (verticalAxis == Direction.Axis.Z ? HEIGHT : WIDTH);
		this.plane = AABB.ofSize(origin, x, y, z);

		Vec3 holeOffset = this.normal.scale(-HOLE_DEPTH);
		this.holeBox = this.plane.expandTowards(holeOffset);
		this.hole = Shapes.create(this.holeBox);
    }

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeVarInt(netId);
		PacketUtils.writeVec3(buf, origin);
		buf.writeEnum(orientation);
		buf.writeEnum(shape);
		buf.writeEnum(type);
		buf.writeVarInt(color);
	}

	public static Portal fromNetwork(FriendlyByteBuf buf) {
		int netId = buf.readVarInt();
		Vec3 origin = PacketUtils.readVec3(buf);
		FrontAndTop orientation = buf.readEnum(FrontAndTop.class);
		PortalShape shape = buf.readEnum(PortalShape.class);
		PortalType type = buf.readEnum(PortalType.class);
		int color = buf.readVarInt();
		return new Portal(netId, origin, orientation, shape, type, color);
	}
}
