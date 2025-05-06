package io.github.fusionflux.portalcubed.content.portal.manager;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.UpdatePortalPairPacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public class ServerPortalManager extends PortalManager {
	public final ServerLevel level;

	public ServerPortalManager(PortalStorage storage, ServerLevel level) {
		super(storage, level);
		this.level = level;
	}

	public void syncToPlayer(ServerPlayer player) {
		for (String key : this.storage.keys()) {
			UpdatePortalPairPacket packet = new UpdatePortalPairPacket(key, this.storage.get(key));
			PortalCubedPackets.sendToClient(player, packet);
		}
	}

	/**
	 * Create a new portal.
	 * If the pair does not exist, a new one is created.
	 * If an old portal already exists, it will be removed.
	 */
	public void createPortal(String key, Polarity polarity, PortalData data) {
		this.modifyPair(key, pair -> pair.with(polarity, new PortalInstance(data)));
	}

	public void removePortal(String key, Polarity polarity) {
		this.modifyPair(key, pair -> pair.without(polarity));
	}

	public void removePortalsInBox(AABB bounds) {
		this.activePortals.getPortals(bounds).forEach(holder -> {
			PortalId id = holder.id();
			this.removePortal(id.key(), id.polarity());
		});
	}

	@Override
	public void setPair(String key, @Nullable PortalPair pair) {
		super.setPair(key, pair);
		UpdatePortalPairPacket packet = new UpdatePortalPairPacket(key, pair);
		PortalCubedPackets.sendToClients(PlayerLookup.world(this.level), packet);
	}

	public static void registerEventListeners() {
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> handler.player.serverLevel().portalManager().syncToPlayer(handler.player)
		);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
				(player, origin, destination) -> destination.portalManager().syncToPlayer(player)
		);
	}
}
