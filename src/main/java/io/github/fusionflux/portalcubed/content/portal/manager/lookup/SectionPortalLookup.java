package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.PortalTransform;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SectionPortalLookup implements PortalLookup, PortalChangeListener {
	private final Long2ObjectMap<List<PortalReference>> sectionsToPortals = new Long2ObjectOpenHashMap<>();

	@Override
	@Nullable
	public PortalHitResult clip(Vec3 from, Vec3 to, int maxDepth) {
		if (this.isEmpty() || maxDepth == 0)
			return null;

		class Closest {
			PortalReference portal = null;
			Vec3 hit = null;
			double distSqr = Double.MIN_VALUE;
		}

		Closest closest = new Closest();
		Vec3 normal = from.vectorTo(to).normalize();

		forEachSectionInBox(from, to, section -> {
			List<PortalReference> portals = this.sectionsToPortals.get(section);
			if (portals == null)
				return;

			for (PortalReference reference : portals) {
				Portal portal = reference.get();

				// only clip when aiming into the front of the portal
				if (portal.normal.dot(normal) >= 0)
					continue;

				Vec3 hit = portal.quad.clip(from, to);
				if (hit == null)
					continue;

				// only hit open portals
				if (!reference.isLinked())
					continue;

				double distSqr = hit.distanceToSqr(from);
				// if first portal, or this hit is closer than prev. closest
				if (closest.portal == null || closest.distSqr > distSqr) {
					closest.portal = reference;
					closest.hit = hit;
					closest.distSqr = distSqr;
				}
			}
		});

		if (closest.portal == null)
			return null;

		PortalReference linked = closest.portal.opposite().orElseThrow(
				() -> new IllegalStateException("Only linked portals should've been found")
		);

		PortalTransform transform = new SinglePortalTransform(closest.portal.get(), linked.get());
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
	public Set<PortalReference> getPortals(AABB bounds) {
		Set<PortalReference> portals = new HashSet<>();

		forEachSectionInBox(bounds, sectionPos -> {
			List<PortalReference> section = this.sectionsToPortals.get(sectionPos);
			if (section != null) {
				section.forEach(reference -> {
					Portal portal = reference.get();
					if (portal.quad.intersects(bounds)) {
						portals.add(reference);
					}
				});
			}
		});

		return portals;
	}

	@Override
	public Set<PortalReference> getPortalsAround(Vec3 pos, double radius) {
		double diameter = radius * 2;
		AABB box = AABB.ofSize(pos, diameter, diameter, diameter);
		Set<PortalReference> set = this.getPortals(box);
		set.removeIf(portal -> !portal.get().quad.intersectsSphere(pos, radius));
		return set;
	}

	@Override
	public boolean isEmpty() {
		return this.sectionsToPortals.isEmpty();
	}

	@Override
	public void portalCreated(PortalReference reference) {
		this.addPortal(reference);
	}

	@Override
	public void portalModified(Portal oldPortal, PortalReference reference) {
		this.removePortal(reference, oldPortal);
		this.addPortal(reference);
	}

	@Override
	public void portalRemoved(PortalReference reference, Portal portal) {
		this.removePortal(reference, portal);
	}

	private void addPortal(PortalReference reference) {
		forEachSectionContainingPortal(
				reference.get(),
				section -> this.sectionsToPortals.computeIfAbsent(section, $ -> new ArrayList<>()).add(reference)
		);
	}

	private void removePortal(PortalReference reference, Portal portal) {
		// reference may be removed, so portal is passed separately
		forEachSectionContainingPortal(portal, section -> {
			List<PortalReference> portals = this.sectionsToPortals.get(section);
			if (portals != null && portals.remove(reference) && portals.isEmpty()) {
				this.sectionsToPortals.remove(section);
			}
		});
	}

	private static void forEachSectionContainingPortal(Portal portal, LongConsumer consumer) {
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
