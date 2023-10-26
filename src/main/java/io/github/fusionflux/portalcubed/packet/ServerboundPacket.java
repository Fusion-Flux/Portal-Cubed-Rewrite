package io.github.fusionflux.portalcubed.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.level.ServerPlayer;

public interface ServerboundPacket extends FabricPacket {
	void handle(ServerPlayer player, PacketSender responder);
}
