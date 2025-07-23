package io.github.fusionflux.portalcubed.content.portal.manager;


import java.util.Collection;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.SectionPortalLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public abstract class PortalManager {
	protected final PortalStorage storage;
	protected final SectionPortalLookup lookup;

	protected PortalManager(PortalStorage storage, Level level) {
		this.storage = storage;
		this.lookup = new SectionPortalLookup();
	}

	@Nullable
	public PortalPair getPair(String key) {
		return this.storage.get(key);
	}

	@Nullable
	public PortalInstance getPortal(PortalId id) {
		PortalPair pair = this.getPair(id.key());
		return pair == null ? null : pair.getNullable(id.polarity());
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

		this.lookup.portalsChanged(key, old, pair);
	}

	public void setPortal(PortalId id, @Nullable PortalData data) {
		this.modifyPair(id.key(), pair -> pair.with(id.polarity(), data));
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

	public PortalLookup lookup() {
		return this.lookup;
	}

	public boolean containsActivePortals(AABB box) {
		for (PortalInstance.Holder portal : this.lookup().getPortals(box)) {
			if (portal.opposite().isPresent()) {
				return true;
			}
		}

		return false;
	}
}
