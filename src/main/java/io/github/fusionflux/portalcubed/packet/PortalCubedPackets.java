package io.github.fusionflux.portalcubed.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import io.github.fusionflux.portalcubed.packet.clientbound.HoldStatusPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenCannonConfigPacket;
import io.github.fusionflux.portalcubed.framework.construct.ConstructSyncPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.CreatePortalPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenSignagePanelConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OtherPlayerShootCannonPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.SimpleParticlePacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureCannonPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenPedestalButtonConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.ShootCannonPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigurePedestalButtonPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.CrowbarSwingPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.DirectClickItemPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.DropPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.GrabPacket;
import net.fabricmc.api.EnvType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class PortalCubedPackets {
	// clientbound
	public static final ResourceLocation OPEN_PEDESTAL_BUTTON_CONFIG = clientbound("open_pedestal_button_config", OpenPedestalButtonConfigPacket::new);
	public static final ResourceLocation CREATE_PORTAL = clientbound("create_portal", CreatePortalPacket::new);
	public static final ResourceLocation SYNC_CONSTRUCTS = clientbound("sync_constructs", ConstructSyncPacket::new);
	public static final ResourceLocation SHOOT_CANNON = clientbound("shoot_cannon", ShootCannonPacket::new);
	public static final ResourceLocation SHOOT_CANNON_OTHER = clientbound("shoot_cannon_other", OtherPlayerShootCannonPacket::new);
	public static final ResourceLocation OPEN_CANNON_CONFIG = clientbound("open_cannon_config", OpenCannonConfigPacket::new);
	public static final ResourceLocation HOLD_STATUS = clientbound("hold_status", HoldStatusPacket::new);
	public static final ResourceLocation DISINTEGRATE = clientbound("disintegrate", DisintegratePacket::new);
	public static final ResourceLocation SIMPLE_PARTICLE = clientbound("simple_particle", SimpleParticlePacket::new);
	public static final ResourceLocation OPEN_LARGE_SIGNAGE_PANEL_CONFIG = clientbound("open_large_signage_panel_config", OpenSignagePanelConfigPacket.Large::new);
	// serverbound
	public static final ResourceLocation CONFIGURE_PEDESTAL_BUTTON = serverbound("configure_pedestal_button", ConfigurePedestalButtonPacket::new);
	public static final ResourceLocation DIRECT_CLICK_ITEM = serverbound("direct_click_item", DirectClickItemPacket::new);
	public static final ResourceLocation CONFIGURE_CANNON = serverbound("configure_cannon", ConfigureCannonPacket::new);
	public static final ResourceLocation GRAB = serverbound("grab", GrabPacket::new);
	public static final ResourceLocation DROP = serverbound("drop", DropPacket::new);
	public static final ResourceLocation CROWBAR_SWING = serverbound("crowbar_swing", CrowbarSwingPacket::new);

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

	private static void registerClientReceiver(ResourceLocation id) {
		ClientPlayNetworking.registerGlobalReceiver(id, ClientboundPacket::receive);
	}

	public static Collection<ServerPlayer> trackingAndSelf(ServerPlayer player) {
		Collection<ServerPlayer> tracking = PlayerLookup.tracking(player);
		List<ServerPlayer> list = new ArrayList<>(tracking);
		list.add(player);
		return list;
	}

	public static <T extends ClientboundPacket> void sendToClient(ServerPlayer player, T packet) {
		ServerPlayNetworking.getSender(player).sendPayload(packet);
	}

	@ClientOnly
	public static <T extends ServerboundPacket> void sendToServer(T packet) {
		ClientPlayNetworking.getSender().sendPayload(packet);
	}
}
