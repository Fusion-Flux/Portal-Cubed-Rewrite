package io.github.fusionflux.portalcubed.content.portal.manager;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.ActivePortalLookup;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.SectionActivePortalLookup;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public abstract class PortalManager {
	public static final Codec<Map<UUID, PortalPair>> PORTALS_CODEC = Codec.unboundedMap(UUIDUtil.CODEC, PortalPair.CODEC);

	private final Level level;

	protected final Map<UUID, PortalPair> portals;
	protected final SectionActivePortalLookup activePortals;

	public PortalManager(Level level) {
		this.level = level;
		this.portals = new HashMap<>();
		this.activePortals = new SectionActivePortalLookup(level);
	}

	public PortalPair getPair(UUID id) {
		return this.portals.get(id);
	}

	public PortalPair getOrCreatePair(UUID id) {
		return this.portals.getOrDefault(id, PortalPair.EMPTY);
	}

	public void setPair(UUID id, @Nullable PortalPair pair) {
		if (pair != null && pair.isEmpty()) {
			pair = null;
		}

		PortalPair old = this.portals.get(id);

		if (pair == null) {
			this.portals.remove(id);
		} else {
			this.portals.put(id, pair);
		}

		this.activePortals.portalsChanged(id, old, pair);
	}

	public void modifyPair(UUID id, UnaryOperator<PortalPair> op) {
		PortalPair pair = this.getOrCreatePair(id);
		this.setPair(id, op.apply(pair));
	}

	public Collection<PortalPair> getAllPairs() {
		return Collections.unmodifiableCollection(this.portals.values());
	}

	public Set<UUID> getAllIds() {
		return Collections.unmodifiableSet(this.portals.keySet());
	}

	public ActivePortalLookup activePortals() {
		return this.activePortals;
	}

	// util used in a couple places
	public boolean isCollisionModified(BlockPos pos) {
		return !this.activePortals().collisionManager().getPatches(pos).isEmpty();
	}

	// TODO: swap to keys directly, this is temporary
	public static UUID generateId(String key) {
		LegacyRandomSource random = new LegacyRandomSource(key.hashCode());
		return new UUID(random.nextLong(), random.nextLong());
	}
}
