package io.github.fusionflux.portalcubed.content.portal.manager;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.phys.Vec3;

public record PortalPickResult(Vec3 start, Vec3 end, Vec3 hit, Portal portal) implements Comparable<PortalPickResult> {
	@Override
	public int compareTo(@NotNull PortalPickResult o) {
		return Double.compare(this.hit.distanceTo(this.start), o.hit.distanceTo(o.start));
	}
}
