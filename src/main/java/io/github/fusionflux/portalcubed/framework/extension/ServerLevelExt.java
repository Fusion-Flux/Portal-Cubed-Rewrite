package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;

public interface ServerLevelExt extends LevelExt {
	ServerPortalManager pc$serverPortalManager();

	@Override
	default PortalManager pc$portalManager() {
		return pc$serverPortalManager();
	}
}
