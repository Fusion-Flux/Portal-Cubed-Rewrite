package io.github.fusionflux.portalcubed.content.portal.storage;

import java.util.List;
import java.util.stream.Stream;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import net.minecraft.world.phys.AABB;

public interface PortalStorage {
	void addPortal(Portal portal);

	void removePortal(Portal portal);

	Stream<Portal> findPortalsInBox(AABB box);

	List<Portal> all();
}
