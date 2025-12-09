package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.UpdatePortalPairPacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class ServerPortalManager extends PortalManager {
	public final ServerLevel level;

	public ServerPortalManager(ServerLevel level) {
		this.level = level;
	}

	public void syncToPlayer(ServerPlayer player) {
		this.forEachPair((key, pair) -> {
			UpdatePortalPairPacket packet = new UpdatePortalPairPacket(key, pair);
			PortalCubedPackets.sendToClient(player, packet);
		});
	}

	/**
	 * Create a new portal.
	 * If the pair does not exist, a new one is created.
	 * If an old portal already exists, it will be removed.
	 */
	public void createPortal(String key, Polarity polarity, PortalData data) {
		this.modifyPair(key, pair -> pair.with(polarity, new Portal(data)));
	}

	public void remove(PortalId id) {
		this.modifyPair(id.key(), pair -> pair.without(id.polarity()));
	}

	public void remove(PortalReference reference) {
		this.remove(reference.id);
	}

	public void removePortalsInBox(AABB bounds) {
		this.lookup().getPortals(bounds).forEach(this::remove);
	}

	@Override
	public void setPair(String key, @Nullable PortalPair newPair) {
		super.setPair(key, newPair);
		UpdatePortalPairPacket packet = new UpdatePortalPairPacket(key, newPair);
		PortalCubedPackets.sendToClients(PlayerLookup.world(this.level), packet);
	}

	@Override
	public void setPortal(PortalId id, @Nullable PortalData data) {
		super.setPortal(id, data);
	}

	@Override
	public void modifyPair(String key, UnaryOperator<PortalPair> op) {
		super.modifyPair(key, op);
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
