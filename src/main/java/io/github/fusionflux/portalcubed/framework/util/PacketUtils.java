package io.github.fusionflux.portalcubed.framework.util;

import java.util.OptionalInt;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class PacketUtils {
	public static void writeOptionalInt(FriendlyByteBuf buf, OptionalInt optionalInt) {
		buf.writeBoolean(optionalInt.isPresent());
		optionalInt.ifPresent(buf::writeVarInt);
	}

	public static OptionalInt readOptionalInt(FriendlyByteBuf buf) {
		return buf.readBoolean() ? OptionalInt.of(buf.readVarInt()) : OptionalInt.empty();
	}

	public static void writeVec3(FriendlyByteBuf buf, Vec3 vec) {
		buf.writeDouble(vec.x);
		buf.writeDouble(vec.y);
		buf.writeDouble(vec.z);
	}

	public static Vec3 readVec3(FriendlyByteBuf buf) {
		return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}
}
