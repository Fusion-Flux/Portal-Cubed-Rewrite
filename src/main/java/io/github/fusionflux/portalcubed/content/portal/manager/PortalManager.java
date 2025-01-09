package io.github.fusionflux.portalcubed.content.portal.manager;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.ActivePortalLookup;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.SectionActivePortalLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class PortalManager {
	protected final Map<String, PortalPair> portals;
	protected final SectionActivePortalLookup activePortals;

	public PortalManager(Level level) {
		this.portals = new HashMap<>();
		this.activePortals = new SectionActivePortalLookup(level);
	}

	public PortalPair getPair(String key) {
		return this.portals.get(key);
	}

	public PortalPair getOrEmpty(String key) {
		return this.portals.getOrDefault(key, PortalPair.EMPTY);
	}

	public void setPair(String key, @Nullable PortalPair pair) {
		if (pair != null && pair.isEmpty()) {
			pair = null;
		}

		PortalPair old = this.portals.get(key);

		if (pair == null) {
			this.portals.remove(key);
		} else {
			this.portals.put(key, pair);
		}

		this.activePortals.portalsChanged(key, old, pair);
	}

	public void modifyPair(String key, UnaryOperator<PortalPair> op) {
		PortalPair pair = this.getOrEmpty(key);
		this.setPair(key, op.apply(pair));
	}

	public Collection<PortalPair> getAllPairs() {
		return Collections.unmodifiableCollection(this.portals.values());
	}

	public Set<String> getAllKeys() {
		return Collections.unmodifiableSet(this.portals.keySet());
	}

	public ActivePortalLookup activePortals() {
		return this.activePortals;
	}

	// util used in a couple places
	public boolean isCollisionModified(BlockPos pos) {
		return !this.activePortals().collisionManager().getPatches(pos).isEmpty();
	}
}
