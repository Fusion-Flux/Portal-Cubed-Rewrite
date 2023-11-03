package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.framework.util.Plane;

public record Portal(PortalManager manager, Plane plane, PortalShape shape, PortalType type) {
}
