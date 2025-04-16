package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.List;

import org.joml.Matrix3d;
import org.joml.Vector3d;

import net.minecraft.core.Rotations;
import net.minecraft.world.entity.Entity;

public record MultiPortalTransform(List<PortalTransform> children) implements PortalTransform {
	@Override
	public Vector3d applyRelative(Vector3d pos) {
		for (PortalTransform child : this.children) {
			child.applyRelative(pos);
		}
		return pos;
	}

	@Override
	public Vector3d applyAbsolute(Vector3d pos) {
		for (PortalTransform child : this.children) {
			child.applyAbsolute(pos);
		}
		return pos;
	}

	@Override
	public Matrix3d apply(Matrix3d rotation) {
		for (PortalTransform child : this.children) {
			child.apply(rotation);
		}
		return rotation;
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
