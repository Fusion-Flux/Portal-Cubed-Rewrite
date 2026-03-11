package io.github.fusionflux.portalcubed.content.portal.interaction;

import io.github.fusionflux.portalcubed.content.PortalCubedGameEvents;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public final class PortalGameEventDispatcher implements PortalChangeListener {
	private static final GameEvent.Context emptyContext = GameEvent.Context.of(null, null);

	private final Level level;

	public PortalGameEventDispatcher(Level level) {
		this.level = level;
	}

	@Override
	public void portalCreated(PortalReference reference) {
		this.dispatchPlace(reference.get().origin());
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		this.dispatchRemove(oldPortal.origin());
		this.dispatchPlace(reference.get().origin());
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.dispatchRemove(portal.origin());
	}

	private void dispatchPlace(Vec3 pos) {
		this.level.gameEvent(PortalCubedGameEvents.PORTAL_PLACE, pos, emptyContext);
	}

	private void dispatchRemove(Vec3 pos) {
		this.level.gameEvent(PortalCubedGameEvents.PORTAL_REMOVE, pos, emptyContext);
	}
}
