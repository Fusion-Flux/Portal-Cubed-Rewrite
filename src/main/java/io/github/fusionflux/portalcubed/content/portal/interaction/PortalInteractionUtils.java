package io.github.fusionflux.portalcubed.content.portal.interaction;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalAware;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.lookup.PortalLookup;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PortalInteractionUtils {
	private PortalInteractionUtils() {}

	/**
	 * Find the path through nearby portals that results in the shortest path to a goal.
	 * @param start the position to start the search from
	 * @param distanceFunction function returning the squared distance between the goal and the given point
	 * @param range the radius around each point where portals can be found
	 * @return the shortest squared distance, or empty if no path was found
	 */
	public static OptionalDouble findPathThroughPortals(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range) {
		double shortestDistSqr = Double.MAX_VALUE;

		PortalLookup lookup = level.portalManager().lookup();
		for (PortalReference portal : lookup.getPortalsAround(start, range)) {
			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty())
				continue;

			double distanceToPortal = start.distanceTo(portal.get().data.origin());
			// this prevents backtracking
			if (distanceToPortal == 0)
				continue;

			double remainingRange = range - distanceToPortal;
			if (remainingRange <= 0)
				continue;

			PortalReference linked = maybeOpposite.get();
			Vec3 newPos = linked.get().data.origin();
			double directDistSqr = distanceFunction.applyAsDouble(newPos);
			double distSqrThroughPortals = findPathThroughPortals(level, newPos, distanceFunction, remainingRange).orElse(Double.MAX_VALUE);
			shortestDistSqr = Math.min(shortestDistSqr, Math.min(directDistSqr, distSqrThroughPortals)) + Mth.square(distanceToPortal);
		}

		return shortestDistSqr == Double.MAX_VALUE ? OptionalDouble.empty() : OptionalDouble.of(shortestDistSqr);
	}

	@Nullable
	public static PortalAware<Void> findPathThroughPortals(Level level, Vec3 start, Vec3 end, double range) {
		return findPathThroughPortalsRecursive(level, start, end, range, new HashSet<>());
	}

	@Nullable
	public static PortalAware<Void> findPathThroughPortalsRecursive(Level level, Vec3 start, Vec3 end, double range, Set<PortalReference> entered) {
		PortalAware<Void> shortest = null;
		double shortestDistSqr = Double.MAX_VALUE;

		for (PortalReference portal : level.portalManager().lookup().getPortalsAround(start, range)) {
			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty())
				continue;

			double distanceToPortal = start.distanceTo(portal.get().data.origin());
			// this prevents backtracking
			if (distanceToPortal == 0)
				continue;

			if (!entered.add(portal))
				continue;

			double remainingRange = range - distanceToPortal;
			if (remainingRange > 0) {
				Vec3 newPos = maybeOpposite.get().get().data.origin();
				double directDistSqr = newPos.distanceToSqr(end);
				PortalAware<Void> path = findPathThroughPortalsRecursive(level, newPos, end, remainingRange, entered);
				double distSqrThroughPortals = path == null ? Double.MAX_VALUE : path.calculateDistanceThroughCenters(newPos, $ -> end);
				if (directDistSqr < shortestDistSqr) {
					shortest = new PortalAware.Tail<>(portal, null);
					shortestDistSqr = Mth.square(distanceToPortal) + directDistSqr;
				}
				if (distSqrThroughPortals < shortestDistSqr) {
					shortest = new PortalAware.Mid<>(portal, path);
					shortestDistSqr = Mth.square(distanceToPortal) + distSqrThroughPortals;
				}
			}

			entered.remove(portal);
		}

		return shortest;
	}

	/**
	 * Portal-aware variant of {@link Level#getEntities(EntityTypeTest, AABB, Predicate) getEntities}.
	 * Does not find entities that <strong>aren't</strong> through portals; both methods must be used.
	 */
	public static <T extends Entity> Set<T> getEntitiesThroughPortals(Level level, EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> predicate) {
		Set<T> set = new HashSet<>();
		getEntitiesThroughPortalsRecursive(level, test, area, predicate, set::add, new HashSet<>());
		return set;
	}

	private static <T extends Entity> void getEntitiesThroughPortalsRecursive(Level level, EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> predicate, Consumer<T> output, Set<PortalReference> entered) {
		for (PortalReference portal : level.portalManager().lookup().getPortals(area)) {
			// empty when not linked
			portal.transform().ifPresent(transform -> {
				if (!entered.add(portal))
					return;

				OBB transformedArea = transform.apply(area);
				for (T found : level.getEntities(test, transformedArea.encompassingAabb, predicate)) {
					// more precise bounds check
					if (transformedArea.intersects(found.getBoundingBox())) {
						output.accept(found);
					}
				}

				getEntitiesThroughPortalsRecursive(level, test, transformedArea.encompassingAabb, predicate, output, entered);
				entered.remove(portal);
			});
		}
	}

	/**
	 * Portal-aware variant of {@link Level#getNearestPlayer(double, double, double, double, boolean) getNearestPlayer}.
	 * Does not find players that <strong>aren't</strong> through portals; both methods must be used.
	 */
	@Nullable
	public static PortalAware<Player> getNearestPlayer(Level level, Vec3 start, double radius, boolean creative) {
		return getNearestPlayerRecursive(level, start, radius, creative, new HashSet<>());
	}

	@Nullable
	private static PortalAware<Player> getNearestPlayerRecursive(Level level, Vec3 start, double radius, boolean creative, Set<PortalReference> entered) {
		PortalAware<Player> nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (PortalReference portal : level.portalManager().lookup().getPortalsAround(start, radius)) {
			// empty when not linked
			Optional<SinglePortalTransform> maybeTransform = portal.transform();
			if (maybeTransform.isEmpty() || !entered.add(portal))
				continue;

			SinglePortalTransform transform = maybeTransform.get();
			Vec3 transformedStart = transform.applyAbsolute(start);
			PortalAware<Player> found = choosePlayerCandidate(portal, level, transformedStart, radius, creative, entered);
			if (found != null) {
				double distance = found.calculateDistanceThroughCenters(start, Player::position);
				if (nearest == null || distance < nearestDistance) {
					nearest = found;
					nearestDistance = distance;
				}
			}

			entered.remove(portal);
		}

		return nearest;
	}

	@Nullable
	private static PortalAware<Player> choosePlayerCandidate(PortalReference portal, Level level, Vec3 pos, double radius, boolean creative, Set<PortalReference> entered) {
		Player player = level.getNearestPlayer(pos.x, pos.y, pos.z, radius, creative);
		if (player != null) {
			Vec3 center = PortalTeleportHandler.centerOf(player);
			Portal exitedPortal = portal.opposite().orElseThrow().get();
			if (exitedPortal.plane.isBehind(center)) {
				// ignore if behind the portal
				player = null;
			}
		}

		PortalAware<Player> throughPortals = getNearestPlayerRecursive(level, pos, radius, creative, entered);
		if (throughPortals != null) {
			Portal exitedPortal = portal.opposite().orElseThrow().get();
			Portal enteredPortal = throughPortals.enteredPortal().get();
			if (exitedPortal.plane.isBehind(enteredPortal.data.origin())) {
				// also ignore if behind the portal
				throughPortals = null;
			}
		}

		if (player == null) {
			return throughPortals == null ? null : new PortalAware.Mid<>(portal, throughPortals);
		} else if (throughPortals == null) {
			return new PortalAware.Tail<>(portal, player);
		}

		double playerDistance = player.position().distanceTo(pos);
		double distanceThroughPortals = throughPortals.calculateDistanceThroughCenters(pos, Player::position);
		if (playerDistance <= distanceThroughPortals) {
			return new PortalAware.Tail<>(portal, player);
		} else {
			return new PortalAware.Mid<>(portal, throughPortals);
		}
	}
}
