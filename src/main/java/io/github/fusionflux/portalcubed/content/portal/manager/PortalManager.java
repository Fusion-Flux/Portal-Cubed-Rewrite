package io.github.fusionflux.portalcubed.content.portal.manager;


import java.util.Collection;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.ActivePortalLookup;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.SectionActivePortalLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class PortalManager {
	protected final PortalStorage storage;
	protected final SectionActivePortalLookup activePortals;

	protected PortalManager(PortalStorage storage, Level level) {
		this.storage = storage;
		this.activePortals = new SectionActivePortalLookup(level);
	}

	public PortalPair getPair(String key) {
		return this.storage.get(key);
	}

	public PortalPair getOrEmpty(String key) {
		return this.storage.getOrEmpty(key);
	}

	public void setPair(String key, @Nullable PortalPair pair) {
		if (pair != null && pair.isEmpty()) {
			pair = null;
		}

		PortalPair old = this.storage.get(key);

		if (pair == null) {
			this.storage.remove(key);
		} else {
			this.storage.put(key, pair);
		}

		this.activePortals.portalsChanged(key, old, pair);
	}

	public void modifyPair(String key, UnaryOperator<PortalPair> op) {
		PortalPair pair = this.getOrEmpty(key);
		this.setPair(key, op.apply(pair));
	}

	public Collection<PortalPair> getAllPairs() {
		return this.storage.values();
	}

	public Set<String> getAllKeys() {
		return this.storage.keys();
	}

	public ActivePortalLookup activePortals() {
		return this.activePortals;
	}

	// util used in a couple places
	public boolean isCollisionModified(BlockPos pos) {
		return !this.activePortals().collisionManager().getPatches(pos).isEmpty();
	}
}
