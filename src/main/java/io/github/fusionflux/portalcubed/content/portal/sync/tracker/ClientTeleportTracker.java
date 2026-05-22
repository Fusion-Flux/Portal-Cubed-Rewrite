package io.github.fusionflux.portalcubed.content.portal.sync.tracker;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.RequestEntitySyncPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/// Tracks teleports on the client, requesting a sync from the server if something goes wrong.
///
/// Valid on all entities except the [local player][LocalPlayer].
public final class ClientTeleportTracker extends TeleportTracker {
	private static final Logger logger = LogUtils.getLogger();

	private final Entity entity;

	public ClientTeleportTracker(Entity entity) {
		this.entity = entity;
	}

	@Override
	protected Entity entity() {
		return this.entity;
	}

	@Override
	protected void syncAfterAbort() {
		logger.warn("Requesting sync for entity {} due to teleport tracking failure", this.entity);
		RequestEntitySyncPacket packet = new RequestEntitySyncPacket(this.entity);
		PortalCubedPackets.sendToServer(packet);
	}
}
