package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPickResult;
import io.github.fusionflux.portalcubed.content.portal.storage.PortalStorage;
import io.github.fusionflux.portalcubed.content.portal.storage.SectionPortalStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class PortalManager {
	protected final PortalStorage storage = new SectionPortalStorage();

	public List<Portal> allPortals() {
		return storage.all();
	}

	@Nullable
	public PortalPickResult pickPortal(Vec3 start, Vec3 end) {
		return storage.findPortalsInBox(new AABB(start, end))
				.map(portal -> this.pickPortal(portal, start, end))
				.filter(Objects::nonNull)
				.sorted().findFirst().orElse(null);
	}

	@Nullable
	private PortalPickResult pickPortal(Portal portal, Vec3 start, Vec3 end) {
//		Vec3 hit = portal.plane.pick(start, end);
//		return hit == null ? null : new PortalPickResult(start, end, hit, portal);
		return null;
	}
}
