package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;

public interface ClientLevelExt extends LevelExt {
	ClientPortalManager pc$clientPortalManager();

	@Override
	default PortalManager pc$portalManager() {
		return pc$clientPortalManager();
	}
}
