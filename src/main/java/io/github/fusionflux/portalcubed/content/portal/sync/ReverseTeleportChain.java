package io.github.fusionflux.portalcubed.content.portal.sync;

import java.util.List;

import net.minecraft.core.Rotations;
import net.minecraft.world.phys.Vec3;

public class ReverseTeleportChain {
	private final List<TrackedTeleport> teleports;

	public ReverseTeleportChain(List<TrackedTeleport> teleports) {
		this.teleports = teleports;
	}

	public Vec3 applyAbsolute(Vec3 pos) {
		for (int i = this.teleports.size() - 1; i >= 0; i--) {
			pos = this.teleports.get(i).transform.inverse.applyAbsolute(pos);
		}
		return pos;
	}

	public Vec3 applyRelative(Vec3 pos) {
		for (int i = this.teleports.size() - 1; i >= 0; i--) {
			pos = this.teleports.get(i).transform.inverse.applyRelative(pos);
		}
		return pos;
	}

	public Rotations apply(float xRot, float yRot) {
		Rotations rotations = new Rotations(xRot, yRot, 0);
		for (int i = this.teleports.size() - 1; i >= 0; i--) {
			rotations = this.teleports.get(i).transform.inverse.apply(rotations);
		}
		return rotations;
	}
}
