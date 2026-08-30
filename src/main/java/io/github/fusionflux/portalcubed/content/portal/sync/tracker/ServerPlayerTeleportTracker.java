package io.github.fusionflux.portalcubed.content.portal.sync.tracker;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.PortalCubedGameEvents;
import io.github.fusionflux.portalcubed.content.PortalCubedStats;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.sync.TrackedTeleport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;

/// Tracks teleports on the server, but only for players.
public final class ServerPlayerTeleportTracker extends TeleportTracker {
	private static final Logger logger = LogUtils.getLogger();

	private final ServerPlayer player;

	public ServerPlayerTeleportTracker(ServerPlayer player) {
		this.player = player;
	}

	@Override
	protected void afterTeleport(TrackedTeleport teleport) {
		PortalReference entered = teleport.pathEntry.entered().reference();
		PortalReference exited = teleport.pathEntry.exited().reference();

		if (entered.isRemoved() || exited.isRemoved()) {
			logger.warn("Cannot dispatch events after teleport, one or both portals have been removed: {}", teleport);
			return;
		}

		ServerLevel level = this.player.level();
		GameEvent.Context context = GameEvent.Context.of(this.player);

		level.gameEvent(PortalCubedGameEvents.PORTAL_TELEPORT_ENTER, entered.get().origin(), context);
		level.gameEvent(PortalCubedGameEvents.PORTAL_TELEPORT_EXIT, exited.get().origin(), context);

		PortalCubedCriteriaTriggers.ENTER_PORTAL.trigger(this.player, entered);
		PortalCubedCriteriaTriggers.ENTER_PORTAL.trigger(this.player, exited);

		this.player.awardStat(PortalCubedStats.PORTALS_TRAVELED_THROUGH);
		this.player.awardStat(switch (entered.id.polarity()) {
			case PRIMARY -> PortalCubedStats.PRIMARY_PORTALS_ENTERED;
			case SECONDARY -> PortalCubedStats.SECONDARY_PORTALS_ENTERED;
		});
	}

	@Override
	protected Entity entity() {
		return this.player;
	}

	@Override
	protected void syncAfterAbort() {
		logger.warn("Syncing player {} due to teleport tracking failure", this.player.getName());

		this.player.connection.teleport(
				this.player.getX(), this.player.getY(), this.player.getZ(),
				this.player.getYRot(), this.player.getXRot()
		);
	}
}
