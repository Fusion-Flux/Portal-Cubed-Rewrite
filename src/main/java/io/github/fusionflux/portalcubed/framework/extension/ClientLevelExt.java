package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.manager.ClientPortalManager;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;

public interface ClientLevelExt extends LevelExt {
	@Override
	default ClientPortalManager portalManager() {
		throw new AbstractMethodError();
	}
}
