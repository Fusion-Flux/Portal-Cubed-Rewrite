package io.github.fusionflux.portalcubed.content.portal.placement;

import org.joml.Quaternionf;

import io.github.fusionflux.portalcubed.framework.util.Angle;
import net.minecraft.world.phys.Vec3;

public record PortalPlacement(Vec3 pos, Quaternionf rotation, Angle rotationAngle) {
}
