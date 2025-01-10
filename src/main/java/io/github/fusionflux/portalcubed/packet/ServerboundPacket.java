package io.github.fusionflux.portalcubed.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ServerboundPacket extends CustomPacketPayload {
	void handle(ServerPlayNetworking.Context ctx);
}
