package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.List;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import net.minecraft.world.entity.Entity;

public record EntityCollisionState(Entity entity, List<PortalInstance.Holder> portals) {
	public EntityCollisionState {
		if (portals.isEmpty()) {
			throw new IllegalArgumentException("An EntityCollisionState should not be created when no portals are present");
		}
	}
}
