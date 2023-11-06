package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.framework.extension.ServerLevelExt;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.CreatePortalPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.LinkPortalsPacket;
import net.minecraft.core.FrontAndTop;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PlayerLookup;

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
	public Portal createPortal(Vec3 pos, FrontAndTop orientation, int color, PortalShape shape, PortalType type) {
		int id = idGenerator.getAndIncrement();
		Portal portal = new Portal(id, pos, orientation, shape, type, color);
		this.storage.addPortal(portal);

		CreatePortalPacket packet = new CreatePortalPacket(portal);
		for (ServerPlayer player : PlayerLookup.world(this.level)) {
			PortalCubedPackets.sendToClient(player, packet);
		}

		return portal;
	}

	public Portal createLinkedPortal(Vec3 pos, FrontAndTop orientation, int color, PortalShape shape, PortalType type, Portal linked) {
		Portal portal = createPortal(pos, orientation, color, shape, type);
		linkPortals(portal, linked);
		return portal;
	}

	@Override
	public void linkPortals(Portal a, Portal b) {
		super.linkPortals(a, b);
		LinkPortalsPacket packet = new LinkPortalsPacket(a, b);
		for (ServerPlayer player : PlayerLookup.world(this.level)) {
			PortalCubedPackets.sendToClient(player, packet);
		}
	}
}
