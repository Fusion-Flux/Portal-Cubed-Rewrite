package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.PortalShape;
import io.github.fusionflux.portalcubed.content.portal.PortalType;
import io.github.fusionflux.portalcubed.framework.util.Plane;

public record Portal(PortalManager manager, Plane plane, PortalShape shape, PortalType type) {
}
