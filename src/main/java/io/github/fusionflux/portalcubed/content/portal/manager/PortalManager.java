package io.github.fusionflux.portalcubed.content.portal.manager;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.ActivePortalLookup;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.SectionActivePortalLookup;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;

public abstract class PortalManager {
	public static final Codec<Map<UUID, PortalPair>> PORTALS_CODEC = Codec.unboundedMap(UUIDUtil.CODEC, PortalPair.CODEC);

	private final Level level;

	protected final Map<UUID, PortalPair> portals;
	protected final SectionActivePortalLookup activePortals;

	public PortalManager(Level level) {
		this.level = level;
		this.portals = new HashMap<>();
		this.activePortals = new SectionActivePortalLookup();
	}

	public PortalPair getPair(UUID id) {
		return this.portals.get(id);
	}

	public PortalPair getOrCreatePair(UUID id) {
		return this.portals.getOrDefault(id, PortalPair.EMPTY);
	}

	public void setPair(UUID id, PortalPair pair) {
		PortalPair old = this.portals.put(id, pair);
		this.activePortals.portalsChanged(old, pair);
	}

	public void modifyPair(UUID id, UnaryOperator<PortalPair> op) {
		PortalPair pair = this.getOrCreatePair(id);
		this.setPair(id, op.apply(pair));
	}

	public Collection<PortalPair> getAllPairs() {
		return this.portals.values();
	}

	public ActivePortalLookup activePortals() {
		return this.activePortals;
	}
}
