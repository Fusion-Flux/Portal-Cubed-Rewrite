package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public final class Portal {
	public final int netId;
    public final Vec3 origin;
	public final FrontAndTop orientation;
    public final PortalShape shape;
    public final PortalType type;
	public final int color;

    public Portal(int netId, Vec3 origin, FrontAndTop orientation, PortalShape shape, PortalType type, int color) {
		this.netId = netId;
        this.origin = origin;
		this.orientation = orientation;
        this.shape = shape;
        this.type = type;
		this.color = color;
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
