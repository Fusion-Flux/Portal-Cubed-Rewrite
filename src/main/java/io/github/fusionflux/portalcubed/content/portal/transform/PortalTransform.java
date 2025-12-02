package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface PortalTransform {
	@Contract(value = "_->param1", mutates = "param1")
	Vector3d applyRelative(Vector3d pos);

	default Vec3 applyRelative(Vec3 pos) {
		Vector3d mutable = new Vector3d(pos.asJoml());
		this.applyRelative(mutable);
		return Vec3Ext.of(mutable);
	}

	@Contract(value = "_->param1", mutates = "param1")
	Vector3d applyAbsolute(Vector3d pos);

	default Vec3 applyAbsolute(Vec3 pos) {
		Vector3d mutable = new Vector3d(pos.asJoml());
		this.applyAbsolute(mutable);
		return Vec3Ext.of(mutable);
	}

	@Contract(value = "_->param1", mutates = "param1")
	Matrix3d apply(Matrix3d rotation);

	Rotations apply(Rotations rotations);

	default Rotations apply(float xRot, float yRot) {
		return this.apply(xRot, yRot, 0);
	}

	default Rotations apply(float xRot, float yRot, float zRot) {
		return this.apply(new Rotations(xRot, yRot, zRot));
	}

	default OBB apply(OBB box) {
		return box.transformed(this::applyAbsolute, this::apply);
	}

	default OBB apply(AABB box) {
		return this.apply(new OBB(box));
	}

	@Contract(mutates = "param1")
	void apply(Entity entity);

	static PortalTransform of(PortalHitResult.Open result) {
		List<PortalTransform> transforms = new ArrayList<>();
		while (result != null) {
			transforms.add(new SinglePortalTransform(result));
			result = result instanceof PortalHitResult.Mid mid && mid.next() instanceof PortalHitResult.Open open ? open : null;
		}

		return transforms.size() == 1 ? transforms.getFirst() : new MultiPortalTransform(transforms);
	}
}
