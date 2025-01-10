package io.github.fusionflux.portalcubed.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientboundPacket extends CustomPacketPayload {
	@Environment(EnvType.CLIENT)
	void handle(ClientPlayNetworking.Context ctx);
}
