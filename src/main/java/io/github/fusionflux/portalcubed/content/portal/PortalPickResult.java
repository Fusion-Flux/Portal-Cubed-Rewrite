package io.github.fusionflux.portalcubed.content.portal;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.phys.Vec3;

public record PortalPickResult(Vec3 start, Vec3 end, Vec3 hit, Portal portal) implements Comparable<PortalPickResult> {
	@Override
	public int compareTo(@NotNull PortalPickResult that) {
		return Double.compare(this.hit.distanceTo(this.start), that.hit.distanceTo(that.start));
	}
}
