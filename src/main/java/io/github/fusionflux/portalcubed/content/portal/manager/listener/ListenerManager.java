package io.github.fusionflux.portalcubed.content.portal.manager.listener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.framework.util.WeakCollection;

public final class ListenerManager implements PortalChangeListener {
	private final List<PortalChangeListener> persistent = new ArrayList<>();
	private final WeakCollection<PortalChangeListener> temporary = new WeakCollection<>();

	/**
	 * Register a new persistent listener. Will never be removed.
	 */
	public void registerPersistent(PortalChangeListener listener) {
		this.persistent.add(listener);
	}

	/**
	 * Register a new temporary listener.
	 * <p>
	 * Temporary listeners are wrapped in a {@link WeakReference},
	 * so they will be removed once they're garbage-collected.
	 */
	public void registerTemporary(PortalChangeListener listener) {
		this.temporary.add(listener);
	}

	@Override
	public void portalCreated(PortalReference reference) {
		this.forEach(listener -> listener.portalCreated(reference));
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		this.forEach(listener -> listener.portalModified(oldPortal, reference));
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.forEach(listener -> listener.portalRemoved(reference, portal));
	}

	private void forEach(Consumer<PortalChangeListener> consumer) {
		this.persistent.forEach(consumer);
		this.temporary.forEach(consumer);
	}
}
