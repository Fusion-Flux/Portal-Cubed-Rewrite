package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.List;

import net.minecraft.core.Rotations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MultiPortalTransform implements PortalTransform {
	private final List<PortalTransform> children;

	public MultiPortalTransform(List<PortalTransform> children) {
		this.children = children;
	}

	@Override
	public Vec3 applyRelative(Vec3 pos) {
		for (PortalTransform child : this.children) {
			pos = child.applyRelative(pos);
		}
		return pos;
	}

	@Override
	public Vec3 applyAbsolute(Vec3 pos) {
		for (PortalTransform child : this.children) {
			pos = child.applyAbsolute(pos);
		}
		return pos;
	}

	@Override
	public Rotations apply(Rotations rotations) {
		for (PortalTransform child : this.children) {
			rotations = child.apply(rotations);
		}
		return rotations;
	}

	@Override
	public void apply(Entity entity) {
		for (PortalTransform child : this.children) {
			child.apply(entity);
		}
	}
}
