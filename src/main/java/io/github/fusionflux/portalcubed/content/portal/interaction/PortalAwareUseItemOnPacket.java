package io.github.fusionflux.portalcubed.content.portal.interaction;

import com.mojang.serialization.DataResult;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.ServerboundPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public record PortalAwareUseItemOnPacket(ServerboundUseItemOnPacket wrapped, PortalPath.Serialized path) implements ServerboundPacket {
	public static final StreamCodec<FriendlyByteBuf, PortalAwareUseItemOnPacket> CODEC = StreamCodec.composite(
			ServerboundUseItemOnPacket.STREAM_CODEC, PortalAwareUseItemOnPacket::wrapped,
			PortalPath.Serialized.STREAM_CODEC, PortalAwareUseItemOnPacket::path,
			PortalAwareUseItemOnPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTAL_AWARE_USE_ITEM_ON;
	}

	@Override
	public void handle(ServerPlayNetworking.Context ctx) {
		PortalManager manager = ctx.player().level().portalManager();
		DataResult<PortalPath> result = this.path.resolve(manager);
		switch (result) {
			case DataResult.Error<?> error -> throw new IllegalStateException("Failed to resolve PortalPath: " + error.message());
			case DataResult.Success<PortalPath> success -> {
				PortalPathHolder path = new PortalPathHolder.Present(success.value());
				this.wrapped.getHitResult().setPortalPath(path);
				this.wrapped.handle(ctx.player().connection);
			}
		}
	}
}
