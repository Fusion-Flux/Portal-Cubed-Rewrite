package io.github.fusionflux.portalcubed.content.portal;

import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public record PortalPickResult(Vec3 start, Vec3 teleportedEnd, Vec3 hitIn, Vec3 hitOut, Portal portal) {
	public static final Comparator<PortalPickResult> CLOSEST_TO_START = Comparator.comparingDouble(
			result -> result.hitIn.distanceTo(result.start)
	);

}
