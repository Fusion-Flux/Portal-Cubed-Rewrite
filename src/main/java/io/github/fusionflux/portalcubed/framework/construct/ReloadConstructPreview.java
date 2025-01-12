package io.github.fusionflux.portalcubed.framework.construct;

import io.github.fusionflux.portalcubed.content.cannon.ConstructPreviewRenderer;
import io.github.fusionflux.portalcubed.packet.ClientboundPacket;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum ReloadConstructPreview implements ClientboundPacket {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ReloadConstructPreview> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(Context ctx) {
		ConstructPreviewRenderer.reload();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PortalCubedPackets.RELOAD_CONSTRUCT_PREVIEW;
	}
}
