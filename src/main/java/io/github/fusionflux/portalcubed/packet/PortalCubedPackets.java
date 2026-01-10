package io.github.fusionflux.portalcubed.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.portal.clear.ClearPortalsPacket;
import io.github.fusionflux.portalcubed.framework.construct.ConstructSyncPacket;
import io.github.fusionflux.portalcubed.framework.construct.ReloadConstructPreview;
import io.github.fusionflux.portalcubed.packet.clientbound.DisintegratePacket;
import io.github.fusionflux.portalcubed.packet.clientbound.HoldStatusPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.LocalTeleportPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenCannonConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenPedestalButtonConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenSignageConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OtherPlayerShootCannonPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.PortalTeleportPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.ShootCannonPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.ShootPortalGunPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.SimpleParticlePacket;
import io.github.fusionflux.portalcubed.packet.clientbound.UpdatePortalPairPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ClientTeleportedPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureCannonPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigurePedestalButtonPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureSignageConfigPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.CrowbarSwingPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.DirectClickItemPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.DropPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.GrabPacket;
import io.github.fusionflux.portalcubed.packet.serverbound.RequestEntitySyncPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class PortalCubedPackets {
	public static final CustomPacketPayload.Type<? extends CustomPacketPayload>
			// clientbound
			UPDATE_PORTAL_PAIR = clientbound("update_portal_pair", UpdatePortalPairPacket.CODEC),
			PORTAL_TELEPORT = clientbound("portal_teleport", PortalTeleportPacket.CODEC),
			LOCAL_TELEPORT = clientbound("local_teleport", LocalTeleportPacket.CODEC),
			OPEN_PEDESTAL_BUTTON_CONFIG = clientbound("open_pedestal_button_config", OpenPedestalButtonConfigPacket.CODEC),
			SYNC_CONSTRUCTS = clientbound("sync_constructs", ConstructSyncPacket.CODEC),
			RELOAD_CONSTRUCT_PREVIEW = clientbound("reload_construct_preview", ReloadConstructPreview.CODEC),
			SHOOT_CANNON = clientbound("shoot_cannon", ShootCannonPacket.CODEC),
			SHOOT_PORTAL_GUN = clientbound("shoot_portal_gun", ShootPortalGunPacket.CODEC),
			SHOOT_CANNON_OTHER = clientbound("shoot_cannon_other", OtherPlayerShootCannonPacket.CODEC),
			OPEN_CANNON_CONFIG = clientbound("open_cannon_config", OpenCannonConfigPacket.CODEC),
			HOLD_STATUS = clientbound("hold_status", HoldStatusPacket.CODEC),
			DISINTEGRATE = clientbound("disintegrate", DisintegratePacket.CODEC),
			SIMPLE_PARTICLE = clientbound("simple_particle", SimpleParticlePacket.CODEC),
			OPEN_LARGE_SIGNAGE_CONFIG = clientbound("open_large_signage_config", OpenSignageConfigPacket.Large.CODEC),
			OPEN_SMALL_SIGNAGE_CONFIG = clientbound("open_small_signage_config", OpenSignageConfigPacket.Small.CODEC),
			// serverbound
			CONFIGURE_PEDESTAL_BUTTON = serverbound("configure_pedestal_button", ConfigurePedestalButtonPacket.CODEC),
			CONFIGURE_LARGE_SIGNAGE = serverbound("configure_large_signage", ConfigureSignageConfigPacket.Large.CODEC),
			CONFIGURE_SMALL_SIGNAGE = serverbound("configure_small_signage", ConfigureSignageConfigPacket.Small.CODEC),
			DIRECT_CLICK_ITEM = serverbound("direct_click_item", DirectClickItemPacket.CODEC),
			CONFIGURE_CANNON = serverbound("configure_cannon", ConfigureCannonPacket.CODEC),
			GRAB = serverbound("grab", GrabPacket.CODEC),
			DROP = serverbound("drop", DropPacket.CODEC),
			CROWBAR_SWING = serverbound("crowbar_swing", CrowbarSwingPacket.CODEC),
			REQUEST_ENTITY_SYNC = serverbound("request_entity_sync", RequestEntitySyncPacket.CODEC),
			CLIENT_TELEPORTED = serverbound("client_teleported", ClientTeleportedPacket.CODEC),
			CLEAR_PORTALS = serverbound("clear_portals", ClearPortalsPacket.CODEC);

	public static void init() {
	}

	private static <T extends ClientboundPacket> CustomPacketPayload.Type<T> clientbound(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(PortalCubed.id(name));
		PayloadTypeRegistry.playS2C().register(type, codec);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(type, ClientboundPacket::handle);
		}

		return type;
	}

	private static <T extends ServerboundPacket> CustomPacketPayload.Type<T> serverbound(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(PortalCubed.id(name));
		PayloadTypeRegistry.playC2S().register(type, codec);
		ServerPlayNetworking.registerGlobalReceiver(type, ServerboundPacket::handle);
		return type;
	}

	public static Collection<ServerPlayer> trackingAndSelf(ServerPlayer player) {
		Collection<ServerPlayer> tracking = PlayerLookup.tracking(player);
		List<ServerPlayer> list = new ArrayList<>(tracking);
		list.add(player);
		return list;
	}

	public static <T extends ClientboundPacket> void sendToClient(ServerPlayer player, T packet) {
		ServerPlayNetworking.send(player, packet);
	}

	public static <T extends ClientboundPacket> void sendToClients(Collection<ServerPlayer> players, T packet) {
		players.forEach(player -> sendToClient(player, packet));
	}

	@Environment(EnvType.CLIENT)
	public static <T extends ServerboundPacket> void sendToServer(T packet) {
		ClientPlayNetworking.send(packet);
	}
}
