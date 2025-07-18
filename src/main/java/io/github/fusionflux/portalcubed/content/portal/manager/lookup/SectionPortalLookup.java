package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SectionPortalLookup implements PortalLookup {
	private final Long2ObjectMap<List<PortalInstance.Holder>> sectionsToPortals = new Long2ObjectOpenHashMap<>();

	@Override
	@Nullable
	public PortalHitResult clip(Vec3 from, Vec3 to, int maxDepth) {
		if (this.isEmpty() || maxDepth == 0)
			return null;

		class Closest {
			PortalInstance.Holder portal = null;
			Vec3 hit = null;
			double distSqr = Double.MIN_VALUE;
		}

		Closest closest = new Closest();
		Vec3 normal = from.vectorTo(to).normalize();

		forEachSectionInBox(from, to, section -> {
			List<PortalInstance.Holder> portals = this.sectionsToPortals.get(section);
			if (portals == null)
				return;

			for (PortalInstance.Holder holder : portals) {
				PortalInstance portal = holder.portal();

				Vec3 hit = portal.visualQuad.clip(from, to);
				if (hit == null)
					continue;

				// only clip when aiming into the front of the portal
				if (portal.normal.dot(normal) >= 0)
					continue;

				double distSqr = hit.distanceToSqr(from);
				// if first portal, or this hit is closer than prev. closest
				if (closest.portal == null || closest.distSqr > distSqr) {
					closest.portal = holder;
					closest.hit = hit;
					closest.distSqr = distSqr;
				}
			}
		});

		if (closest.portal == null)
			return null;

		Optional<PortalInstance.Holder> linked = closest.portal.opposite();

		if (linked.isEmpty()) {
			return new PortalHitResult.Closed(closest.portal, closest.hit);
		}

		PortalTransform transform = new SinglePortalTransform(closest.portal.portal(), linked.get().portal());
		Vec3 teleportedHit = transform.applyAbsolute(closest.hit);
		Vec3 teleportedEnd = transform.applyAbsolute(to);
		PortalHitResult next = this.clip(teleportedHit, teleportedEnd, maxDepth - 1);

		if (next == null) {
			return new PortalHitResult.Tail(closest.portal, closest.hit, teleportedHit, teleportedEnd);
		} else {
			return new PortalHitResult.Mid(closest.portal, closest.hit, teleportedHit, next);
		}
	}

	@Override
	public List<PortalInstance.Holder> getPortals(AABB bounds) {
		List<PortalInstance.Holder> portals = new ArrayList<>();

		forEachSectionInBox(bounds, sectionPos -> {
			List<PortalInstance.Holder> section = this.sectionsToPortals.get(sectionPos);
			if (section != null) {
				section.forEach(holder -> {
					PortalInstance portal = holder.portal();
					if (portal.visualQuad.intersects(bounds)) {
						portals.add(holder);
					}
				});
			}
		});

		return portals;
	}

	@Override
	public boolean isEmpty() {
		return this.sectionsToPortals.isEmpty();
	}

	public void portalsChanged(String pairKey, @Nullable PortalPair oldPair, @Nullable PortalPair newPair) {
		if (oldPair != null) {
			PortalPair.Holder holder = new PortalPair.Holder(pairKey, oldPair);
			for (PortalInstance.Holder portal : holder) {
				forEachSectionContainingPortal(portal.portal(), section -> {
					List<PortalInstance.Holder> portals = this.sectionsToPortals.get(section);
					if (portals != null && portals.remove(portal) && portals.isEmpty()) {
						this.sectionsToPortals.remove(section);
					}
				});
			}
		}
		if (newPair != null) {
			PortalPair.Holder holder = new PortalPair.Holder(pairKey, newPair);
			for (PortalInstance.Holder portal : holder) {
				forEachSectionContainingPortal(
						portal.portal(),
						section -> this.sectionsToPortals.computeIfAbsent(section, $ -> new ArrayList<>()).add(portal)
				);
			}
		}
	}

	private static void forEachSectionContainingPortal(PortalInstance portal, LongConsumer consumer) {
		forEachSectionInBox(portal.visualQuad.containingBox(), consumer);
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
