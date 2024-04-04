package io.github.fusionflux.portalcubed.packet;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.packet.clientbound.CreatePortalPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenPedestalButtonConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.PropHoldPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigurePedestalButtonPacket;
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
	public static final ResourceLocation OPEN_PEDESTAL_BUTTON_CONFIG = clientbound("open_pedestal_button_config", OpenPedestalButtonConfigPacket::new);
	public static final ResourceLocation PROP_HOLD = clientbound("prop_hold", PropHoldPacket::new);
	public static final ResourceLocation CREATE_PORTAL = clientbound("create_portal", CreatePortalPacket::new);
	// serverbound
	public static final ResourceLocation CONFIGURE_PEDESTAL_BUTTON = serverbound("configure_pedestal_button", ConfigurePedestalButtonPacket::new);
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
