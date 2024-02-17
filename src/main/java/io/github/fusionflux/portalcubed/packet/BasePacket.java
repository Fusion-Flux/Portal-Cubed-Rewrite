package io.github.fusionflux.portalcubed.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface BasePacket extends CustomPacketPayload {
	@FunctionalInterface
	interface Factory<T extends BasePacket> extends FriendlyByteBuf.Reader<T> {
		T create(FriendlyByteBuf buf);

		@Override
		default T apply(FriendlyByteBuf buf) {
			return create(buf);
		}
	}
}
