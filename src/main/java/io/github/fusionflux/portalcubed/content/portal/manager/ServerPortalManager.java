package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.framework.extension.ServerLevelExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.CreatePortalPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.LinkPortalsPacket;
import net.minecraft.core.FrontAndTop;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import io.github.fusionflux.portalcubed.packet.clientbound.RemovePortalPacket;

import org.quiltmc.qsl.networking.api.PlayerLookup;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPortalManager extends PortalManager {
	public final ServerLevel level;

	private static final AtomicInteger idGenerator = new AtomicInteger();

	public ServerPortalManager(ServerLevel level) {
		this.level = level;
	}

	public static ServerPortalManager of(ServerLevel level) {
		return ((ServerLevelExt) level).pc$serverPortalManager();
	}

	/**
	 * Create and sync a new portal.
	 */
	public Portal createPortal(Vec3 pos, FrontAndTop orientation, int color, PortalShape shape, PortalType type, UUID player) {
		// potentially remove old portal
		PortalPair playerPortals = this.getPortalsOf(player);
		playerPortals.getOptional(type).ifPresent(this::removePortal);
		// create new portal
		int id = idGenerator.getAndIncrement();
		Portal portal = new Portal(id, pos, orientation, shape, type, color, player);
		// store new portal
		this.storage.addPortal(portal);
		// (re-)link
		this.linkPortals(playerPortals);
		// notify clients
		PortalCubedPackets.sendToClients(PlayerLookup.world(level), new CreatePortalPacket(portal));

		return portal;
	}

	private void removePortal(Portal portal) {
		unlinkPortal(portal);
		this.storage.removePortal(portal);
		PortalCubedPackets.sendToClients(PlayerLookup.world(level), new RemovePortalPacket(portal));
	}

	@Override
	public void linkPortals(Portal a, Portal b) {
		super.linkPortals(a, b);
		PortalCubedPackets.sendToClients(PlayerLookup.world(level), new LinkPortalsPacket(a, b));
	}
}
