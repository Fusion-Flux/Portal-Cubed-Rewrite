package io.github.fusionflux.portalcubed.content.portal.manager;

import java.util.stream.Stream;

import net.minecraft.world.phys.AABB;

public interface PortalStorage {
	void addPortal(Portal portal);

	Stream<Portal> findPortalsInBox(AABB box);
}
