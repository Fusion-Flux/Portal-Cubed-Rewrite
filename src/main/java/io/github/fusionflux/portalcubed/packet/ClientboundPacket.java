package io.github.fusionflux.portalcubed.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.player.LocalPlayer;

import org.quiltmc.loader.api.minecraft.ClientOnly;

public interface ClientboundPacket extends FabricPacket {
	@ClientOnly
	void handle(LocalPlayer player, PacketSender responder);
}
