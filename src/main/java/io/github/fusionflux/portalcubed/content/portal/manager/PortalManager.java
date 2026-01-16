package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import io.github.fusionflux.portalcubed.content.portal.Polarity;
import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.collision.RePortaler;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.ListenerManager;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.SectionPortalLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public abstract sealed class PortalManager permits ServerPortalManager, ClientPortalManager {
	private final Map<String, PortalPair> pairs;
	private final Map<PortalId, PortalReference> references;
	private final ListenerManager listeners;
	private final PortalLookup lookup;

	protected PortalManager(Level level) {
		this.pairs = new HashMap<>();
		this.references = new HashMap<>();
		this.listeners = new ListenerManager();

		SectionPortalLookup lookup = new SectionPortalLookup();
		this.lookup = lookup;
		this.listeners.registerPersistent(lookup);
		this.listeners.registerPersistent(new RePortaler(level));
	}

	@Nullable
	public PortalReference getPortal(PortalId id) {
		return this.references.get(id);
	}

	public Optional<PortalReference> getPortalOptional(PortalId id) {
		return Optional.ofNullable(this.getPortal(id));
	}

	public PortalReference getPortalOrThrow(PortalId id) {
		PortalReference portal = this.getPortal(id);
		if (portal == null) {
			throw new NoSuchElementException("Portal does not exist: " + id);
		}
		return portal;
	}

	@Nullable
	public PortalPair getPair(String key) {
		return this.pairs.get(key);
	}

	public PortalPair getPairOrEmpty(String key) {
		return this.pairs.getOrDefault(key, PortalPair.EMPTY);
	}

	protected void setPair(String key, @Nullable PortalPair newPair) {
		if (newPair == null) {
			newPair = PortalPair.EMPTY;
		}

		PortalPair oldPair = this.getPairOrEmpty(key);
		if (newPair.equals(oldPair)) {
			// no need to do anything
			return;
		}

		this.listeners.portalPairChanged(oldPair, newPair);

		for (Polarity polarity : Polarity.values()) {
			Portal oldPortal = oldPair.getNullable(polarity);
			Portal newPortal = newPair.getNullable(polarity);

			if (Objects.equals(oldPortal, newPortal)) {
				// either both are null, or the two are identical. no change, so we can exit early.
				continue;
			}

			PortalId id = new PortalId(key, polarity);

			if (oldPortal == null) {
				// newPortal must be non-null, since they're not equal.
				// portal added, create a new reference.
				PortalReference reference = new PortalReference(id, this, newPortal);
				if (this.references.put(id, reference) != null) {
					throw new IllegalStateException("Duplicate reference for portal: " + id);
				}
				this.listeners.portalCreated(reference);
			} else if (newPortal == null) {
				// oldPortal must be non-null, otherwise first case would've been hit.
				// portal removed, remove reference.
				PortalReference reference = this.references.remove(id);
				Objects.requireNonNull(reference, () -> "Missing reference for portal: " + id);

				reference.update(null);
				this.listeners.portalRemoved(reference, oldPortal);
			} else {
				// both non-null, portal modified
				PortalReference reference = this.getPortal(id);
				Objects.requireNonNull(reference, () -> "Missing reference for portal: " + id);

				reference.update(newPortal);
				this.listeners.portalModified(oldPortal, reference);
			}
		}

		if (newPair.isEmpty()) {
			// when a pair is cleared, make sure not to keep a dead entry around forever
			this.pairs.remove(key);
		} else {
			this.pairs.put(key, newPair);
		}
	}

	protected void setPortal(PortalId id, @Nullable PortalData data) {
		this.modifyPair(id.key(), pair -> pair.with(id.polarity(), data));
	}

	protected void modifyPair(String key, UnaryOperator<PortalPair> op) {
		PortalPair pair = this.getPairOrEmpty(key);
		this.setPair(key, op.apply(pair));
	}

	@UnmodifiableView
	public Collection<PortalReference> portals() {
		return Collections.unmodifiableCollection(this.references.values());
	}

	@UnmodifiableView
	public Map<String, PortalPair> pairs() {
		return Collections.unmodifiableMap(this.pairs);
	}

	public void forEachPair(BiConsumer<String, PortalPair> consumer) {
		this.pairs.forEach(consumer);
	}

	public PortalLookup lookup() {
		return this.lookup;
	}

	/**
	 * @return the {@link ListenerManager}, which allows for registering {@link PortalChangeListener}s
	 */
	public ListenerManager listeners() {
		return this.listeners;
	}

	public boolean containsActivePortals(AABB box) {
		for (PortalReference portal : this.lookup().getPortals(box)) {
			if (portal.isLinked()) {
				return true;
			}
		}

		return false;
	}
}
