package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.collision.CollisionManager;

public interface LevelExt {
	PortalManager pc$portalManager();
	CollisionManager pc$collisionManager();
}
