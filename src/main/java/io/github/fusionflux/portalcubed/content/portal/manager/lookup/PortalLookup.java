package io.github.fusionflux.portalcubed.content.portal.manager.lookup;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface PortalLookup {
	/**
	 * Raycast between two points, hitting portals in between.
	 * @return null if no portals were hit
	 */
	@Nullable
	PortalHitResult clip(Vec3 from, Vec3 to);

	List<PortalInstance.Holder> getPortals(AABB bounds);

	boolean isEmpty();
}
