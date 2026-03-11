package io.github.fusionflux.portalcubed.content.portal.interaction.packet;

import java.util.Optional;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;
import io.github.fusionflux.portalcubed.framework.util.ScopedValueAtHome;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public final class PortalAwareInteractPacket extends ServerboundPortalAwarePacket<ServerboundInteractPacket> {
	public static final StreamCodec<FriendlyByteBuf, PortalAwareInteractPacket> CODEC = codec(ServerboundInteractPacket.STREAM_CODEC, PortalAwareInteractPacket::new);

	private static final ScopedValueAtHome<PortalPath> portalPath = ScopedValueAtHome.newInstance();

	public PortalAwareInteractPacket(ServerboundInteractPacket wrapped, PortalPath.Serialized path) {
		super(wrapped, path);
	}

	public PortalAwareInteractPacket(ServerboundInteractPacket wrapped, PortalPath path) {
		this(wrapped, path.serialize());
	}

	@Override
	protected void wrapHandle(PortalPathHolder holder, Runnable handler) {
		if (!(holder instanceof PortalPathHolder.Present(PortalPath path))) {
			handler.run();
			return;
		}

		portalPath.runWhere(path, handler);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.PORTAL_AWARE_INTERACT;
	}

	public static Optional<PortalPath> currentPath() {
		return portalPath.isBound() ? Optional.of(portalPath.get()) : Optional.empty();
	}
}
