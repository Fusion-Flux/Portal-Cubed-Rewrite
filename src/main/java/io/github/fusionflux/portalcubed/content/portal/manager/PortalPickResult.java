package io.github.fusionflux.portalcubed.content.portal.manager;

import net.minecraft.world.phys.Vec3;

public record PortalPickResult(Vec3 start, Vec3 end, Vec3 hit, Portal portal) {
}
