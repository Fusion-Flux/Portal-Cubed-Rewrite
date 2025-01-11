package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.gun.GrabSoundManager;

public interface AbstractClientPlayerExt {
	default GrabSoundManager grabSoundManager() {
		throw new AbstractMethodError();
	}
}
