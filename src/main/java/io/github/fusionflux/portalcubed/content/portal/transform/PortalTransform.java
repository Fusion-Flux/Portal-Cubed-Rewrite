package io.github.fusionflux.portalcubed.content.portal.transform;

import java.util.ArrayList;
import java.util.List;

import io.github.fusionflux.portalcubed.content.portal.PortalHitResult;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface PortalTransform {
	Vec3 applyRelative(Vec3 pos);

	Vec3 applyAbsolute(Vec3 pos);

	Rotations apply(Rotations rotations);

	default Rotations apply(float xRot, float yRot) {
		return this.apply(xRot, yRot, 0);
	}

	default Rotations apply(float xRot, float yRot, float zRot) {
		return this.apply(new Rotations(xRot, yRot, zRot));
	}

	void apply(Entity entity);

	static PortalTransform of(PortalHitResult result) {
		List<PortalTransform> transforms = new ArrayList<>();
		transforms.add(new SinglePortalTransform(result.in(), result.out()));
		while (result.hasNext()) {
			PortalHitResult next = result.next();
			transforms.add(new SinglePortalTransform(next.in(), next.out()));
			result = next;
		}
		return transforms.size() == 1 ? transforms.getFirst() : new MultiPortalTransform(transforms);
	}
}
