package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;

public interface LevelExt {
	// note: no prefix needed
	default PortalManager portalManager() {
		throw new AbstractMethodError();
	}

	default PortalCubedDamageSources pc$damageSources() {
		throw new AbstractMethodError();
	}
}
