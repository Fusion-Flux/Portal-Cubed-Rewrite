package io.github.fusionflux.portalcubed.framework.util;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public record Plane(Vec3 pos1, Vec3 pos2) {
	@Nullable
	public Vec3 pick(Vec3 start, Vec3 end) {
		return null; // TODO
	}
}
