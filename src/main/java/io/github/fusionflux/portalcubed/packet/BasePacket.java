package io.github.fusionflux.portalcubed.packet;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface BasePacket extends CustomPacketPayload {
	@FunctionalInterface
	interface Factory<T extends BasePacket> extends FriendlyByteBuf.Reader<T> {
		T create(FriendlyByteBuf buf);

		@Override
		default T apply(FriendlyByteBuf buf) {
			return create(buf);
		}
	}

	ResourceLocation getId();

	@Override
	@NotNull // I don't want to add this to every subclass
	default ResourceLocation id() {
		return this.getId();
	}
}
