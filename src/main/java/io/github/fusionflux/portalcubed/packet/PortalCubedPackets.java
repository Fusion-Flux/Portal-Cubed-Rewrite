package io.github.fusionflux.portalcubed.packet;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.packet.serverbound.DirectClickItemPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.server.level.ServerPlayer;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

import java.util.function.Function;

public class PortalCubedPackets {
	public static final PacketType<DirectClickItemPacket> DIRECT_CLICK_ITEM = serverbound("direct_click_item", DirectClickItemPacket::new);

	public static void init() {
	}

	private static <T extends ClientboundPacket> PacketType<T> clientbound(String name, Function<FriendlyByteBuf, T> factory) {
		PacketType<T> type = PacketType.create(PortalCubed.id(name), factory);
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			registerClientReceiver(type);
		}
		return type;
	}

	private static <T extends ServerboundPacket> PacketType<T> serverbound(String name, Function<FriendlyByteBuf, T> factory) {
		PacketType<T> type = PacketType.create(PortalCubed.id(name), factory);
		ServerPlayNetworking.registerGlobalReceiver(type, ServerboundPacket::handle);
		return type;
	}

	private static <T extends ClientboundPacket> void registerClientReceiver(PacketType<T> type) {
		ClientPlayNetworking.registerGlobalReceiver(type, ClientboundPacket::handle);
	}

	public static <T extends FabricPacket> void sendToClient(ServerPlayer player, T packet) {
		ServerPlayNetworking.send(player, packet);
	}

	@ClientOnly
	public static <T extends FabricPacket> void sendToServer(T packet) {
		ClientPlayNetworking.send(packet);
	}
}
