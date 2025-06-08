package io.github.fusionflux.portalcubed.framework.shape;

import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.content.portal.placement.PortalableSurface;
import io.github.fusionflux.portalcubed.framework.shape.flat.Line2d;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.world.phys.Vec3;

public record Line(Vec3 from, Vec3 to) {
	public Line(Vector3dc from, Vector3dc to) {
		this(TransformUtils.toMc(from), TransformUtils.toMc(to));
	}

	public Line moved(Vec3 offset) {
		return new Line(this.from.add(offset), this.to.add(offset));
	}

	public Line2d to2d(PortalableSurface surface) {
		return new Line2d(surface.to2d(this.from), surface.to2d(this.to));
	}
}
