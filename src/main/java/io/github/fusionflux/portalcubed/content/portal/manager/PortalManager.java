package io.github.fusionflux.portalcubed.content.portal.manager;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PortalManager {
	private final Level level;
	private final PortalStorage storage;

	public PortalManager(Level level) {
		this.level = level;
		this.storage = new PortalStorage();
	}

	public void addPortal(Portal portal) {
	}

	@Nullable
	public PortalPickResult pickPortal(Vec3 start, Vec3 end) {
		Portal closestToStart = null;
		double closestDist = Double.MAX_VALUE;
		Vec3 closestHit = null;
		for (Iterator<Portal> itr = storage.findPortalsInBox(start, end); itr.hasNext();) {
			Portal portal = itr.next();
			Vec3 hit = portal.plane().pick(start, end);
			if (hit != null) {
				double dist = hit.distanceTo(start);
				if (dist < closestDist) {
					closestToStart = portal;
					closestDist = dist;
					closestHit = hit;
				}
			}
		}

		return closestToStart == null ? null : new PortalPickResult(start, end, closestHit, closestToStart);
	}
}
