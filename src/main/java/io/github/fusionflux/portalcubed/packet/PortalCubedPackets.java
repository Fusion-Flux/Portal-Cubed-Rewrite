package io.github.fusionflux.portalcubed.packet;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.construct.ConstructSyncPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.CreatePortalPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.DirectClickItemPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.KeyPressPacket;
import net.fabricmc.api.EnvType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class PortalCubedPackets {
	// clientbound
	public static final ResourceLocation CREATE_PORTAL = clientbound("create_portal", CreatePortalPacket::new);
	public static final ResourceLocation SYNC_CONSTRUCTS = clientbound("sync_constructs", ConstructSyncPacket::new);
	// serverbound
	public static final ResourceLocation DIRECT_CLICK_ITEM = serverbound("direct_click_item", DirectClickItemPacket::new);
	public static final ResourceLocation KEY_PRESS = serverbound("key_press", KeyPressPacket::new);

	public static void init() {
	}

	private static <T extends ClientboundPacket> ResourceLocation clientbound(String name, ClientboundPacket.Factory<T> factory) {
		var id = PortalCubed.id(name);
		CustomPayloads.registerS2CPayload(id, factory);
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT)
			registerClientReceiver(id);
		return id;
	}

	private static <T extends ServerboundPacket> ResourceLocation serverbound(String name, ServerboundPacket.Factory<T> factory) {
		var id = PortalCubed.id(name);
		CustomPayloads.registerC2SPayload(id, factory);
		ServerPlayNetworking.registerGlobalReceiver(id, ServerboundPacket::receive);
		return id;
	}

	private static <T extends ClientboundPacket> void registerClientReceiver(ResourceLocation id) {
		ClientPlayNetworking.registerGlobalReceiver(id, ClientboundPacket::receive);
	}

	public static <T extends ClientboundPacket> void sendToClient(ServerPlayer player, T packet) {
		ServerPlayNetworking.getSender(player).sendPayload(packet);
	}

	@ClientOnly
	public static <T extends ServerboundPacket> void sendToServer(T packet) {
		ClientPlayNetworking.getSender().sendPayload(packet);
	}
}
