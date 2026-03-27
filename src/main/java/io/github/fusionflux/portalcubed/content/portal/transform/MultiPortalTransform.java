package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import net.minecraft.core.Rotations;
import net.minecraft.world.entity.Entity;

public final class MultiPortalTransform implements PortalTransform {
	private final List<SinglePortalTransform> steps;
	private final MultiPortalTransform inverse;

	public MultiPortalTransform(List<SinglePortalTransform> steps) {
		this.steps = List.copyOf(steps);
		this.inverse = new MultiPortalTransform(this);
	}

	private MultiPortalTransform(MultiPortalTransform inverse) {
		this.inverse = inverse;
		this.steps = new ArrayList<>();
		for (SinglePortalTransform step : inverse.steps) {
			this.steps.addFirst(step.inverse());
		}
	}

	@Override
	public MultiPortalTransform inverse() {
		return this.inverse;
	}

	@Override
	public MultiPortalTransform andThen(PortalTransform next) {
		List<SinglePortalTransform> steps = new ArrayList<>(this.steps);
		next.forEachStep(steps::add);
		return new MultiPortalTransform(steps);
	}

	@Override
	public void forEachStep(Consumer<SinglePortalTransform> consumer) {
		this.steps.forEach(consumer);
	}

	@Override
	public Vector3d applyRelative(Vector3d pos) {
		for (SinglePortalTransform step : this.steps) {
			step.applyRelative(pos);
		}
		return pos;
	}

	@Override
	public Vector3d applyAbsolute(Vector3d pos) {
		for (SinglePortalTransform step : this.steps) {
			step.applyAbsolute(pos);
		}
		return pos;
	}

	@Override
	public Matrix3d apply(Matrix3d rotation) {
		for (SinglePortalTransform step : this.steps) {
			step.apply(rotation);
		}
		return rotation;
	}

	@Override
	public Quaternionf apply(Quaternionf rotation) {
		for (SinglePortalTransform step : this.steps) {
			step.apply(rotation);
		}
		return rotation;
	}

	@Override
	public Rotations apply(Rotations rotations) {
		for (SinglePortalTransform step : this.steps) {
			rotations = step.apply(rotations);
		}
		return rotations;
	}

	@Override
	public void apply(Entity entity) {
		for (SinglePortalTransform step : this.steps) {
			step.apply(entity);
		}
	}
}
