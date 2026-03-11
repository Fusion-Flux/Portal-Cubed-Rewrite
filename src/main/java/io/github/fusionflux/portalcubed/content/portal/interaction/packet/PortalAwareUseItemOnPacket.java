package io.github.fusionflux.portalcubed.content.portal.interaction.packet;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public final class PortalAwareUseItemOnPacket extends ServerboundPortalAwarePacket<ServerboundUseItemOnPacket> {
	public static final StreamCodec<FriendlyByteBuf, PortalAwareUseItemOnPacket> CODEC = codec(ServerboundUseItemOnPacket.STREAM_CODEC, PortalAwareUseItemOnPacket::new);

	public PortalAwareUseItemOnPacket(ServerboundUseItemOnPacket wrapped, PortalPath.Serialized path) {
		super(wrapped, path);
	}

	public PortalAwareUseItemOnPacket(ServerboundUseItemOnPacket wrapped, PortalPath path) {
		this(wrapped, path.serialize());
	}

	@Override
	protected void wrapHandle(PortalPathHolder path, Runnable handler) {
		this.wrapped.getHitResult().setPortalPath(path);
		handler.run();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTAL_AWARE_USE_ITEM_ON;
	}
}
