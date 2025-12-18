package io.github.fusionflux.portalcubed.content.portal.command.argument.portal;

import io.github.fusionflux.portalcubed.content.portal.graphics.PortalType;
import net.minecraft.core.Holder;

public record PortalInput(Holder<PortalType> type, PortalAttributeMap attributes) {
}
