package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Quaternionf;

import net.minecraft.world.phys.Vec3;

public record Plane(Quaternionf rotation, Vec3 center, double width, double height) {

}
