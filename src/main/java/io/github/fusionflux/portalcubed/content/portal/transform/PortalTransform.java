package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3d;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface PortalTransform {
	Vector3d applyRelative(Vector3d pos);

	default Vec3 applyRelative(Vec3 pos) {
		return TransformUtils.toMc(this.applyRelative(TransformUtils.toJoml(pos)));
	}

	Vector3d applyAbsolute(Vector3d pos);

	default Vec3 applyAbsolute(Vec3 pos) {
		return TransformUtils.toMc(this.applyAbsolute(TransformUtils.toJoml(pos)));
	}

	Matrix3d apply(Matrix3d rotation);

	Rotations apply(Rotations rotations);

	default Rotations apply(float xRot, float yRot) {
		return this.apply(xRot, yRot, 0);
	}

	default Rotations apply(float xRot, float yRot, float zRot) {
		return this.apply(new Rotations(xRot, yRot, zRot));
	}

	void apply(Entity entity);

	static PortalTransform of(PortalHitResult result) {
		PortalTransform first = new SinglePortalTransform(result.in(), result.out());
		if (!result.hasNext())
			return first;

		List<PortalTransform> transforms = new ArrayList<>();
		transforms.add(first);
		while (result.hasNext()) {
			PortalHitResult next = result.next();
			transforms.add(new SinglePortalTransform(next.in(), next.out()));
			result = next;
		}

		return new MultiPortalTransform(transforms);
	}
}
