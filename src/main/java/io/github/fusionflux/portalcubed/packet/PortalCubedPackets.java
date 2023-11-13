package io.github.fusionflux.portalcubed.packet;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.packet.clientbound.CreatePortalPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.LinkPortalsPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.RemovePortalPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.PlainTeleportPacket;
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

import java.util.Collection;
import java.util.function.Function;

public class PortalCubedPackets {
	// clientbound
	public static final PacketType<CreatePortalPacket> CREATE_PORTAL = clientbound("create_portal", CreatePortalPacket::new);
	public static final PacketType<RemovePortalPacket> REMOVE_PORTAL = clientbound("remove_portal", RemovePortalPacket::new);
	public static final PacketType<LinkPortalsPacket> LINK_PORTALS = clientbound("link_portals", LinkPortalsPacket::new);
	public static final PacketType<PlainTeleportPacket> PLAIN_TELEPORT = clientbound("plain_teleport", PlainTeleportPacket::new);
	// serverbound
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

	public static <T extends FabricPacket> void sendToClients(Collection<ServerPlayer> players, T packet) {
		for (ServerPlayer player : players) {
			sendToClient(player, packet);
		}
	}

	@ClientOnly
	public static <T extends FabricPacket> void sendToServer(T packet) {
		ClientPlayNetworking.send(packet);
	}
}
