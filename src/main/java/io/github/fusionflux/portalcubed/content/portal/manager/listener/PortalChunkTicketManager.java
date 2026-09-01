package io.github.fusionflux.portalcubed.content.portal.manager.listener;

import io.github.fusionflux.portalcubed.content.PortalCubedMisc;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/// Manages chunk load tickets as portals are created and removed.
public final class PortalChunkTicketManager implements PortalChangeListener {
	// based on Ender Pearls, but have a lifespan of 0 (persistent) since we will handle removal
	public static final int RADIUS = 2;

	private final ServerLevel level;

	public PortalChunkTicketManager(ServerLevel level) {
		this.level = level;
	}

	@Override
	public void portalCreated(PortalReference reference) {
		this.addTicket(pos(reference.get()));
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		Portal newPortal = reference.get();

		ChunkPos oldChunkPos = ChunkPos.containing(oldPortal.blockPos());
		ChunkPos newChunkPos = ChunkPos.containing(newPortal.blockPos());
		if (oldChunkPos.equals(newChunkPos))
			return;

		this.removeTicket(oldChunkPos);
		this.addTicket(newChunkPos);
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.removeTicket(pos(portal));
	}

	private void addTicket(ChunkPos pos) {
		this.level.getChunkSource().addTicketAndLoadWithRadius(PortalCubedMisc.PORTAL_CHUNK_TICKET_TYPE, pos, RADIUS);
	}

	private void removeTicket(ChunkPos pos) {
		this.level.getChunkSource().removeTicketWithRadius(PortalCubedMisc.PORTAL_CHUNK_TICKET_TYPE, pos, RADIUS);
	}

	private static ChunkPos pos(Portal portal) {
		return ChunkPos.containing(portal.blockPos());
	}
}
