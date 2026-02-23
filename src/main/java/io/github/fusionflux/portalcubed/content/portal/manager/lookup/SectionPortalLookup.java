package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.manager.listener.PortalChangeListener;
import io.github.fusionflux.portalcubed.content.portal.ref.HitPortal;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
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
	public PortalHitResult clip(Vec3 from, Vec3 to, int recursionLimit, boolean hitClosed) {
		if (recursionLimit < 0) {
			throw new IllegalArgumentException("Recursion limit must be >=0: " + recursionLimit);
		} else if (this.isEmpty()) {
			return PortalHitResult.EMPTY;
		}

		List<PortalPath.Entry> enteredPortals = new ArrayList<>();
		Vec3 currentStart = from;
		Vec3 currentEnd = to;
		int i = 0;

		while (true) {
			HitPortal hit = this.clip(currentStart, currentEnd, hitClosed);
			if (hit == null)
				break;

			Optional<PortalReference> maybeLinked = hit.reference().opposite();
			if (maybeLinked.isEmpty()) {
				// hit a closed portal
				Validate.isTrue(hitClosed, "Hit a closed portal, hitClosed must be true");
				return new PortalHitResult(enteredPortals, hit);
			}

			if (i >= recursionLimit) {
				// too many loops, early exit
				return new PortalHitResult(enteredPortals, hit);
			}

			PortalReference linked = maybeLinked.get();
			SinglePortalTransform transform = new SinglePortalTransform(hit.reference().get(), linked.get());

			currentStart = transform.applyAbsolute(hit.pos());
			currentEnd = transform.applyAbsolute(currentEnd);

			enteredPortals.add(new PortalPath.Entry(hit, new HitPortal(linked, currentStart)));
			i++;
		}

		// finished looping with no trailing portal
		return new PortalHitResult(enteredPortals, null);
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

	@Nullable
	private HitPortal clip(Vec3 start, Vec3 end, boolean hitClosed) {
		ClosestPortal candidate = new ClosestPortal();
		Vec3 direction = start.vectorTo(end).normalize();

		forEachSectionInBox(start, end, section -> {
			List<PortalReference> portals = this.sectionsToPortals.get(section);
			if (portals == null)
				return;

			for (PortalReference reference : portals) {
				Portal portal = reference.get();

				// only clip when aiming into the front of the portal
				if (portal.normal.dot(direction) >= 0)
					continue;

				Vec3 hit = portal.quad.clip(start, end);
				if (hit == null)
					continue;

				if (!hitClosed && !reference.isLinked())
					continue;

				double distSqr = hit.distanceToSqr(start);
				candidate.consider(reference, hit, distSqr);
			}
		});

		return candidate.finish();
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

	private static final class ClosestPortal {
		private PortalReference portal;
		private Vec3 hit;
		private double distSqr;

		private ClosestPortal() {
			this.reset();
		}

		private void consider(PortalReference portal, Vec3 hit, double distSqr) {
			if (this.portal == null || distSqr < this.distSqr) {
				this.portal = portal;
				this.hit = hit;
				this.distSqr = distSqr;
			}
		}

		private void reset() {
			this.portal = null;
			this.hit = null;
			this.distSqr = Double.MAX_VALUE;
		}

		@Nullable
		private HitPortal finish() {
			if (this.portal == null) {
				Validate.isTrue(this.hit == null, "No portal found, hit should be null");
				Validate.isTrue(this.distSqr == Double.MAX_VALUE, "No portal found, distance should be default");
			} else {
				Validate.isTrue(this.hit != null, "Portal found, hit should be present");
				Validate.isTrue(this.distSqr != Double.MAX_VALUE, "Portal found, distance should be set");
			}

			return this.portal == null ? null : new HitPortal(this.portal, this.hit);
		}
	}
}
