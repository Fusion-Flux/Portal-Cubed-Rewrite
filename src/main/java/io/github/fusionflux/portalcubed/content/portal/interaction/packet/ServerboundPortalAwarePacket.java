package io.github.fusionflux.portalcubed.content.portal.interaction.packet;

import java.util.function.BiFunction;

import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public abstract class ServerboundPortalAwarePacket<T extends Packet<ServerGamePacketListener>> implements ServerboundPacket {
	protected final T wrapped;
	protected final PortalPath.Serialized path;

	protected ServerboundPortalAwarePacket(T wrapped, PortalPath.Serialized path) {
		this.wrapped = wrapped;
		this.path = path;
	}

	@Override
	public final void handle(ServerPlayNetworking.Context ctx) {
		PortalManager manager = ctx.player().level().portalManager();
		DataResult<PortalPath> result = this.path.resolve(manager);
		switch (result) {
			case DataResult.Error<?> error -> throw new IllegalStateException("Failed to resolve PortalPath: " + error.message());
			case DataResult.Success<PortalPath> success -> {
				PortalPathHolder path = new PortalPathHolder.Present(success.value());
				this.wrapHandle(path, () -> this.wrapped.handle(ctx.player().connection));
			}
		}
	}

	protected abstract void wrapHandle(PortalPathHolder path, Runnable handler);

	// this is terrible, I know
	protected static
	<B extends ByteBuf, P extends Packet<ServerGamePacketListener>, T extends ServerboundPortalAwarePacket<P>>
	StreamCodec<B, T> codec(StreamCodec<? super B, P> wrappedCodec, BiFunction<P, PortalPath.Serialized, T> factory) {
		return StreamCodec.composite(
				wrappedCodec, packet -> packet.wrapped,
				PortalPath.Serialized.STREAM_CODEC, packet -> packet.path,
				factory
		);
	}
}
