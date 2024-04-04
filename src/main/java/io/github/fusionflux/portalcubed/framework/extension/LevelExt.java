package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.PortalCubedDamageSources;

public interface LevelExt {
	PortalCubedDamageSources pc$damageSources();

	default void pc$playSoundInstance(Object soundInstance) {}
}
