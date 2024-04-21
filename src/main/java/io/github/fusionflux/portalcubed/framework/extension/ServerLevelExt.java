package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;

public interface ServerLevelExt extends LevelExt {
	@Override
	default ServerPortalManager portalManager() {
		throw new AbstractMethodError();
	}
}
