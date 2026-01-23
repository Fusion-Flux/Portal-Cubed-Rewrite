package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.util.Color;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Common code used in multiple portal-collision-related places.
 */
public final class PortalCollisionUtils {
	private PortalCollisionUtils() {}

	/**
	 * Invoke a callback with each collision box found on the other side of a linked portal.
	 * The callback may return {@code false} to cancel iteration.
	 * @throws IllegalArgumentException if the given portal is not linked
	 */
	public static void forEachBoxOnOtherSide(Entity entity, PortalReference portal, AABB area, Predicate<OBB> consumer) {
		PortalReference linked = portal.opposite().orElseThrow(() -> new IllegalArgumentException("Portal is not linked"));
		SinglePortalTransform transform = new SinglePortalTransform(portal.get(), linked.get());
		AABB transformedArea = transform.apply(area).encompassingAabb;

		DebugRendering.addBox(1, transformedArea, Color.PURPLE);

		for (VoxelShape shape : entity.level().getCollisions(null, transformedArea)) {
			for (AABB box : shape.toAabbs()) {
				if (linked.get().hides(box))
					continue;

				OBB transformed = transform.inverse().apply(box);
				if (!consumer.test(transformed)) {
					return;
				}
			}
		}
	}
}
