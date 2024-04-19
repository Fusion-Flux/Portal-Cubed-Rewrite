package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;

public interface LevelExt {
	PortalManager pc$portalManager();

	PortalCubedDamageSources pc$damageSources();

	default void pc$playSoundInstance(Object soundInstance) {}
}
