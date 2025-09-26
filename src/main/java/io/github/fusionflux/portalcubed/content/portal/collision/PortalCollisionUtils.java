package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.List;
import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
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
	 * Find any portals that are "relevant" for collision.
	 * This includes portals that:
	 * <ul>
	 *     <li>intersect the given area</li>
	 *     <li>are linked</li>
	 *     <li>have the given entity in their entity-affecting area</li>
	 * </ul>
	 */
	public static List<PortalInstance.Holder> findRelevantPortalsFor(Entity entity, AABB searchArea) {
		List<PortalInstance.Holder> portals = entity.level().portalManager().lookup().getPortals(searchArea);
		portals.removeIf(portal -> {
			if (!portal.pair().pair().isLinked())
				return true;

			return !portal.portal().seesModifiedCollision(entity);
		});
		return portals;
	}

	/**
	 * Invoke a callback with each collision box found on the other side of a linked portal.
	 * The callback may return {@code false} to cancel iteration.
	 * @throws IllegalArgumentException if the given portal is not linked
	 */
	public static void forEachBoxOnOtherSide(Entity entity, PortalInstance.Holder portal, AABB area, Predicate<OBB> consumer) {
		PortalInstance.Holder linked = portal.opposite().orElseThrow(() -> new IllegalArgumentException("Portal is not linked"));
		SinglePortalTransform transform = new SinglePortalTransform(portal.portal(), linked.portal());
		AABB transformedArea = transform.apply(area).encompassingAabb;

		DebugRendering.addBox(1, transformedArea, Color.PURPLE);

		for (VoxelShape shape : entity.level().getCollisions(entity, transformedArea)) {
			for (AABB box : shape.toAabbs()) {
				if (linked.portal().plane.isBehind(box))
					continue;

				OBB transformed = transform.inverse.apply(box);
				if (!consumer.test(transformed)) {
					return;
				}
			}
		}
	}
}
