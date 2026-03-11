package io.github.fusionflux.portalcubed.framework.extension;

import io.github.fusionflux.portalcubed.content.portal.ref.PortalPathHolder;

public interface HitResultExt {
	default PortalPathHolder portalPath() {
		throw new AbstractMethodError();
	}

	default void setPortalPath(PortalPathHolder holder) {
		throw new AbstractMethodError();
	}
}
