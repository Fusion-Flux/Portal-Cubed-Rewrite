package io.github.fusionflux.portalcubed.content.portal.manager;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class PortalManager {
	public static final Codec<Map<UUID, PortalPair>> PORTALS_CODEC = Codec.unboundedMap(UUIDUtil.CODEC, PortalPair.CODEC);

	private final Level level;

	protected final Map<UUID, PortalPair> portals = new HashMap<>();

	public PortalManager(Level level) {
		this.level = level;
	}

	public PortalPair getPair(UUID id) {
		return this.portals.get(id);
	}

	public void modifyPair(UUID id, UnaryOperator<PortalPair> op) {
		PortalPair pair = this.getPair(id);
		this.portals.put(id, op.apply(pair));
	}

	public Collection<PortalPair> getAllPairs() {
		return this.portals.values();
	}


}
