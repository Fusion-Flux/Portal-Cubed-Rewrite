package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.storage.PortalStorage;
import io.github.fusionflux.portalcubed.content.portal.storage.SectionPortalStorage;
import io.github.fusionflux.portalcubed.framework.extension.LevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PortalManager {
	protected final PortalStorage storage = new SectionPortalStorage();

	public List<Portal> allPortals() {
		return storage.all();
	}

	public static PortalManager of(Level level) {
		return ((LevelExt) level).pc$portalManager();
	}

	@Nullable
	public Portal getPortalByNetId(int id) {
		return storage.getByNetId(id);
	}

	public Set<Portal> getPortalsAt(BlockPos pos) {
		return storage.findPortalsInBox(new AABB(pos).inflate(0.1)).collect(Collectors.toSet());
	}

	@Nullable
	public PortalHitResult clipPortal(Vec3 start, Vec3 end) {
		return storage.findPortalsInBox(new AABB(start, end))
                .map(portal -> this.clipPortal(portal, start, end))
                .filter(Objects::nonNull)
				.min(PortalHitResult.CLOSEST_TO_START)
				.orElse(null);
	}

	@Nullable
	private PortalHitResult clipPortal(Portal portal, Vec3 start, Vec3 end) {
		Portal linked = portal.getLinked();
		if (linked == null) // this shouldn't happen
			return null;
		// portals cannot be interacted with from behind
		Vec3 delta = end.subtract(start);
		if (delta.dot(portal.normal) > 0)
			return null;
		return portal.plane.clip(start, end).map(hit -> {
//			portal.
//			Vec3 teleportedEnd = end;
//			Vec3 hitOut = hit;
            return new PortalHitResult(start, end, hit, hit, portal, linked);
        }).orElse(null);
	}

	public void linkPortals(Portal a, Portal b) {
		unlinkPortal(a);
		unlinkPortal(b);
		a.setLinked(b);
		b.setLinked(a);
	}

	public void unlinkPortal(Portal portal) {
		Portal linked = portal.getLinked();
		if (linked != null) {
			portal.setLinked(null);
			linked.setLinked(null);
		}
	}
}
