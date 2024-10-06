package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.collision.CollisionManager;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.longs.LongConsumer;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SectionActivePortalLookup implements ActivePortalLookup {
	private final Long2ObjectMap<List<PortalInstance>> sectionsToPortals = new Long2ObjectOpenHashMap<>();
	private final Map<PortalInstance, PortalPair> portalsToPairs = new HashMap<>();
	private final CollisionManager collisionManager;

	public SectionActivePortalLookup(Level level) {
		this.collisionManager = new CollisionManager(level);
	}

	@Override
	@Nullable
	public PortalHitResult clip(Vec3 from, Vec3 to) {
		if (this.isEmpty())
			return null;

		class Closest {
			PortalInstance portal = null;
			Vec3 hit = null;
			double distSqr = Double.MIN_VALUE;
		}

		final Closest closest = new Closest();
		Vec3 normal = from.vectorTo(to).normalize();

		forEachSectionInBox(from, to, section -> {
			List<PortalInstance> portals = this.sectionsToPortals.get(section);
			if (portals == null)
				return;

			for (PortalInstance portal : portals) {
				Vec3 hit = portal.quad.clip(from, to);
				if (hit == null)
					continue;

				// only clip when aiming into the front of the portal
				if (portal.normal.dot(normal) < 0)
					continue;

				double distSqr = hit.distanceToSqr(from);
				// if first portal, or this hit is closer than prev. closest
				if (closest.portal == null || closest.distSqr > distSqr) {
					closest.portal = portal;
					closest.hit = hit;
					closest.distSqr = distSqr;
				}
			}
		});

		if (closest.portal == null)
			return null;

		PortalPair pair = this.portalsToPairs.get(closest.portal);
		PortalInstance linked = pair.other(closest.portal);
		// only paired portals should be stored
		Objects.requireNonNull(linked);

		Vec3 teleportedHit = PortalTeleportHandler.teleportAbsoluteVecBetween(closest.hit, closest.portal, linked);
		Vec3 teleportedEnd = PortalTeleportHandler.teleportAbsoluteVecBetween(to, closest.portal, linked);

		PortalHitResult next;
		try {
			next = this.clip(teleportedHit, teleportedEnd);
		} catch (StackOverflowError e) {
			System.out.println("stack overflow");
			return PortalHitResult.OVERFLOW_MARKER;
		}

		return new PortalHitResult(
				from,
				next == null ? teleportedEnd : null,
				closest.portal, linked, pair,
				closest.hit, teleportedHit,
				next
		);
	}

	@Override
	public boolean isEmpty() {
		return this.sectionsToPortals.isEmpty();
	}

	@Override
	public CollisionManager collisionManager() {
		return this.collisionManager;
	}

	public void portalsChanged(@Nullable PortalPair oldPair, @Nullable PortalPair newPair) {
		if (oldPair != null && oldPair.isLinked()) {
			for (PortalInstance portal : oldPair) {
				this.portalsToPairs.remove(portal);
				forEachSectionContainingPortal(portal, section -> {
					List<PortalInstance> portals = this.sectionsToPortals.get(section);
					if (portals != null && portals.remove(portal) && portals.isEmpty()) {
						this.sectionsToPortals.remove(section);
					}
				});
			}
			this.collisionManager.removePair(oldPair);
		}
		if (newPair != null && newPair.isLinked()) {
			for (PortalInstance portal : newPair) {
				this.portalsToPairs.put(portal, newPair);
				forEachSectionContainingPortal(
						portal,
						section -> this.sectionsToPortals.computeIfAbsent(section, $ -> new ArrayList<>()).add(portal)
				);

			}
			this.collisionManager.addPair(newPair);
		}
	}

	private static void forEachSectionContainingPortal(PortalInstance portal, LongConsumer consumer) {
		forEachSectionInBox(portal.quad.containingBox(), consumer);
	}

	private static void forEachSectionInBox(Vec3 cornerA, Vec3 cornerB, LongConsumer consumer) {
		double minX = Math.min(cornerA.x, cornerB.x);
		double minY = Math.min(cornerA.y, cornerB.y);
		double minZ = Math.min(cornerA.z, cornerB.z);
		double maxX = Math.max(cornerA.x, cornerB.x);
		double maxY = Math.max(cornerA.y, cornerB.y);
		double maxZ = Math.max(cornerA.z, cornerB.z);
		forEachSectionInBox(minX, minY, minZ, maxX, maxY, maxZ, consumer);
	}

	private static void forEachSectionInBox(AABB box, LongConsumer consumer) {
		forEachSectionInBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, consumer);
	}

	private static void forEachSectionInBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, LongConsumer consumer) {
		int minSectionX = SectionPos.blockToSectionCoord(minX);
		int minSectionY = SectionPos.blockToSectionCoord(minY);
		int minSectionZ = SectionPos.blockToSectionCoord(minZ);
		int maxSectionX = SectionPos.blockToSectionCoord(maxX);
		int maxSectionY = SectionPos.blockToSectionCoord(maxY);
		int maxSectionZ = SectionPos.blockToSectionCoord(maxZ);

		// todo: maybe unroll this manually
		for (int x = minSectionX; x <= maxSectionX; x++) {
			for (int y = minSectionY; y <= maxSectionY; y++) {
				for (int z = minSectionZ; z <= maxSectionZ; z++) {
					consumer.accept(SectionPos.asLong(x, y, z));
				}
			}
		}
	}
}
