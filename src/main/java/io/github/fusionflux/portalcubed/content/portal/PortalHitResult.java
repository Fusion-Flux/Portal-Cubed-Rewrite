package io.github.fusionflux.portalcubed.content.portal;

import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public record PortalHitResult(Vec3 start, Vec3 teleportedEnd, Vec3 hitIn, Vec3 hitOut, Portal in, Portal out) {
	public static final Comparator<PortalHitResult> CLOSEST_TO_START = Comparator.comparingDouble(
			result -> result.hitIn.distanceTo(result.start)
	);

	public Vec3 teleportAbsoluteVec(Vec3 pos) {
		return PortalTeleportHandler.teleportAbsoluteVecBetween(pos, in, out);
	}

	public Vec3 teleportRelativeVec(Vec3 vec) {
		return PortalTeleportHandler.teleportRelativeVecBetween(vec, in, out);
	}
}
