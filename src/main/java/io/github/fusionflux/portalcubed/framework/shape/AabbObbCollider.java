package io.github.fusionflux.portalcubed.framework.shape;

import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.util.Color;
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
	 * @param motionVector the motion vector, which will be modified if a collision occurs
	 * @param onHit a callback that will be invoked each time a box is collided with
	 * @return true if a collision occurred, otherwise false
	 */
	public boolean collide(AABB bounds, Vector3d motionVector, Consumer<OBB> onHit) {
		Direction.Axis[] axes = orderedAxes(motionVector);

		boolean collisionOccurred = false;

		for (Direction.Axis axis : axes) {
			double motion = Maath.get(motionVector, axis);
			if (motion == 0)
				continue;

			boolean collided = false;
			for (OBB box : this.boxes) {
				double allowed = box.collide(bounds, axis, motion);
				if (allowed != motion) {
					collided = true;
					onHit.accept(box);
					DebugRendering.addBox(1, box, Color.YELLOW);

					// only change the target if this collision results in a closer hit
					if (Math.abs(allowed) < Math.abs(motion)) {
						motion = allowed;
					}
				}

				if (allowed == 0) {
					// no need to check the other boxes
					break;
				}
			}

			if (!collided) {
				// update the bounds and exit early
				bounds = Maath.move(bounds, axis, motion);
				continue;
			}

			collisionOccurred = true;
			Maath.set(motionVector, axis, motion);

			if (motion != 0) {
				bounds = Maath.move(bounds, axis, motion);
			}
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
