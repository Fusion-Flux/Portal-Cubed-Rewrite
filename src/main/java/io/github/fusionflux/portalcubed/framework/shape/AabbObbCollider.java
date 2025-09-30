package io.github.fusionflux.portalcubed.framework.shape;

import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.util.Maath;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * Collides an AABB moving along a motion vector with multiple OBBs.
 */
public final class AabbObbCollider {
	private static final Direction.Axis[] xFirst = { Direction.Axis.Y, Direction.Axis.X, Direction.Axis.Z };
	public static final Direction.Axis[] zFirst = { Direction.Axis.Y, Direction.Axis.X, Direction.Axis.Z };

	private final List<OBB> boxes;

	public AabbObbCollider(List<OBB> boxes) {
		this.boxes = boxes;
	}

	/**
	 * Collide the given bounds moving along the given motion vector with all boxes.
	 * @param motion the motion vector, which will be modified if a collision occurs
	 * @param onHit a callback that will be invoked each time a box is collided with
	 * @return true if a collision occurred, otherwise false
	 */
	public boolean collide(AABB bounds, Vector3d motion, Consumer<OBB> onHit) {
		Direction.Axis[] axes = orderedAxes(motion);

		boolean collisionOccurred = false;

		// we need to be able to restart the axis iteration to account for deflections
		outer: while (true) {
			for (Direction.Axis axis : axes) {
				double target = Maath.get(motion, axis);
				if (target == 0)
					continue;

				OBB.Result nearestHit = null;
				for (OBB box : this.boxes) {
					OBB.Result result = box.collide(bounds, axis, target);
					if (result != null) {
						onHit.accept(box);
						if (nearestHit == null || result.actual() < nearestHit.actual()) {
							nearestHit = result;
						}
					}
				}

				if (nearestHit == null) {
					bounds = Maath.move(bounds, axis, target);
					continue;
				}

				collisionOccurred = true;

				double actual = nearestHit.actual();
				Maath.set(motion, axis, actual);

				if (actual != 0) {
					bounds = Maath.move(bounds, axis, actual);
				}

				if (nearestHit.deflection().lengthSquared() > 0) {
					motion.add(nearestHit.deflection());
					continue outer;
				}
			}

			// if we reach this, all axes have been collided along successfully.
			break;
		}

		return collisionOccurred;
	}

	/**
	 * Vanilla always collides with Y first, but chooses X or Z based on which axis has the most motion.
	 * @see Entity#collideWithShapes
	 */
	@SuppressWarnings("JavadocReference")
	private static Direction.Axis[] orderedAxes(Vector3dc motion) {
		return Math.abs(motion.x()) < Math.abs(motion.z()) ? zFirst : xFirst;
	}
}
