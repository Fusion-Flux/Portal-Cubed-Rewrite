package io.github.fusionflux.portalcubed.content.portal.placement;

import org.joml.Quaternionf;

import net.minecraft.world.phys.Vec3;

public record PortalPlacement(Vec3 pos, Quaternionf rotation) {
}
