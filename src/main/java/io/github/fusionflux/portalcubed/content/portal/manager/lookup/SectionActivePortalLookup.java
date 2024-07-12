package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

public class SectionActivePortalLookup implements ActivePortalLookup {
	private final Long2ObjectMap<List<PortalInstance>> sectionsToPortals = new Long2ObjectOpenHashMap<>();
	private final Map<PortalInstance, PortalPair> portalsToPairs = new HashMap<>();

	@Override
	@Nullable
	public PortalHitResult clip(Vec3 from, Vec3 to) {
		if (this.isEmpty())
			return null;

		int fromSectionX = SectionPos.blockToSectionCoord(from.x);
		int fromSectionY = SectionPos.blockToSectionCoord(from.y);
		int fromSectionZ = SectionPos.blockToSectionCoord(from.z);
		int toSectionX = SectionPos.blockToSectionCoord(to.x);
		int toSectionY = SectionPos.blockToSectionCoord(to.y);
		int toSectionZ = SectionPos.blockToSectionCoord(to.z);

		PortalInstance closest = null;
		Vec3 closestHit = null;
		double closestDistSqr = Double.MAX_VALUE;

		for (int x = fromSectionX; x <= toSectionX; x++) {
			for (int z = fromSectionZ; z <= toSectionZ; z++) {
				for (int y = fromSectionY; y <= toSectionY; y++) {
					long packed = SectionPos.asLong(x, y, z);
					List<PortalInstance> portals = this.sectionsToPortals.get(packed);
					if (portals == null)
						continue;

					for (PortalInstance portal : portals) {
						Vec3 hit = portal.quad.clip(from, to);
						// it hit, and the hit point is the closest so far
						if (hit != null && (closest == null || closestDistSqr > hit.distanceToSqr(from))) {
							closest = portal;
							closestHit = hit;
						}
					}
				}
			}
		}

		if (closest == null)
			return null;

		PortalPair pair = this.portalsToPairs.get(closest);
		PortalInstance linked = pair.other(closest);

		Vec3 teleportedHit = PortalTeleportHandler.teleportAbsoluteVecBetween(closestHit, closest, linked);
		Vec3 teleportedEnd = PortalTeleportHandler.teleportAbsoluteVecBetween(to, closest, linked);

		return new PortalHitResult(
				from, closestHit,
				closest, linked, pair,
				closestHit, teleportedHit,
				this.clip(teleportedHit, teleportedEnd)
		);
	}

	@Override
	public boolean isEmpty() {
		return this.sectionsToPortals.isEmpty();
	}

	public void portalsChanged(@Nullable PortalPair oldPair, @Nullable PortalPair newPair) {
		if (oldPair != null && oldPair.isLinked()) {
			for (PortalInstance portal : oldPair) {
				this.portalsToPairs.remove(portal);
				// TODO: iterate sections intersected by plane
			}
		}
		if (newPair != null && newPair.isLinked()) {
			for (PortalInstance portal : newPair) {
				this.portalsToPairs.put(portal, newPair);
				// TODO: iterate sections intersected by plane
			}
		}
	}
}
