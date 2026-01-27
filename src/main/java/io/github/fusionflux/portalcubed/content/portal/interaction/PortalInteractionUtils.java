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
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalPath;
import io.github.fusionflux.portalcubed.content.portal.ref.PortalReference;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PortalInteractionUtils {
	private PortalInteractionUtils() {}

	/**
	 * @see #findPathLengthSqrThroughPortals(Level, Vec3, ToDoubleFunction, double)
	 */
	public static OptionalDouble findPathLengthSqrThroughPortals(Level level, Vec3 start, Vec3 end, double range) {
		return findPathLengthSqrThroughPortals(level, start, end::distanceTo, range);
	}

	/**
	 * {@link #findPathThroughPortals(Level, Vec3, ToDoubleFunction, double) find a path through portals}, and then get its length if found.
	 */
	public static OptionalDouble findPathLengthSqrThroughPortals(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range) {
		PortalPath path = findPathThroughPortals(level, start, distanceFunction, range);
		return path == null ? OptionalDouble.empty() : OptionalDouble.of(path.lengthSqr(start, distanceFunction));
	}

	/**
	 * @see #findPathThroughPortals(Level, Vec3, ToDoubleFunction, double)
	 */
	@Nullable
	public static PortalPath findPathThroughPortals(Level level, Vec3 start, Vec3 end, double range) {
		return findPathThroughPortals(level, start, end::distanceTo, range);
	}

	/**
	 * Find a path from a start point to a target that passes through portals.
	 * @param distanceFunction a function providing the distance from a given point to the target
	 * @param range the maximum length of a found path
	 */
	@Nullable
	public static PortalPath findPathThroughPortals(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range) {
		return findPathThroughPortalsRecursive(level, start, distanceFunction, range, new HashSet<>(), new HashSet<>());
	}

	@Nullable
	public static PortalPath findPathThroughPortalsRecursive(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range, Set<String> seenPairs, Set<PortalReference> noPath) {
		PortalPath shortest = null;
		double shortestDistance = Double.MAX_VALUE;

		for (PortalReference portal : level.portalManager().lookup().getPortalsAround(start, range)) {
			if (portal.get().plane.isBehind(start) || noPath.contains(portal))
				continue;

			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty() || !seenPairs.add(portal.id.key()))
				continue;

			double distanceToPortal = start.distanceTo(portal.get().data.origin());
			double remainingRange = range - distanceToPortal;
			if (remainingRange > 0) {
				PortalReference linked = maybeOpposite.get();
				Vec3 newPos = linked.get().data.origin();
				double directDistance = distanceToPortal + distanceFunction.applyAsDouble(newPos);

				PortalPath throughPortals = findPathThroughPortalsRecursive(level, newPos, distanceFunction, remainingRange, seenPairs, noPath);
				double distThroughPortals = throughPortals == null ? Double.MAX_VALUE : throughPortals.length(newPos, distanceFunction);
				if (throughPortals == null)
					noPath.add(portal);

				if (directDistance < shortestDistance) {
					shortest = PortalPath.of(portal, linked);
					shortestDistance = directDistance;
				}

				if (distThroughPortals < shortestDistance) {
					shortest = throughPortals.prepend(portal, linked);
					shortestDistance = distThroughPortals;
				}
			}

			seenPairs.remove(portal.id.key());
		}

		return shortest;
	}

	/**
	 * Portal-aware variant of {@link Level#getEntities(EntityTypeTest, AABB, Predicate) getEntities}.
	 * Does not find entities that <strong>aren't</strong> through portals; both methods must be used.
	 * @return a new, mutable Set of found entities
	 */
	public static <T extends Entity> Set<T> getEntitiesThroughPortals(Level level, EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> predicate) {
		Set<T> found = new HashSet<>();
		getEntitiesThroughPortalsRecursive(level, test, area, predicate, found::add, new HashSet<>());
		return found;
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
	public static PortalPath.With<Player> getNearestPlayer(Level level, Vec3 start, double radius, boolean creative) {
		return getNearestPlayerRecursive(level, start, radius, creative, new HashSet<>(), new HashSet<>());
	}

	@Nullable
	private static PortalPath.With<Player> getNearestPlayerRecursive(Level level, Vec3 start, double radius, boolean creative, Set<PortalReference> entered, Set<PortalReference> noPath) {
		PortalPath.With<Player> nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (PortalReference portal : level.portalManager().lookup().getPortalsAround(start, radius)) {
			if (portal.get().plane.isBehind(start) || noPath.contains(portal))
				continue;

			// empty when not linked
			Optional<SinglePortalTransform> maybeTransform = portal.transform();
			if (maybeTransform.isEmpty() || !entered.add(portal))
				continue;

			SinglePortalTransform transform = maybeTransform.get();
			Vec3 transformedStart = transform.applyAbsolute(start);
			PortalPath.With<Player> found = choosePlayerCandidate(portal, level, transformedStart, radius, creative, entered, noPath);
			if (found != null) {
				double distance = found.path().length(start, found.value().position());
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
	private static PortalPath.With<Player> choosePlayerCandidate(PortalReference portal, Level level, Vec3 pos, double radius, boolean creative, Set<PortalReference> entered, Set<PortalReference> noPath) {
		Player player = level.getNearestPlayer(pos.x, pos.y, pos.z, radius, creative);
		if (player != null) {
			Vec3 center = PortalTeleportHandler.centerOf(player);
			Portal exitedPortal = portal.opposite().orElseThrow().get();
			if (exitedPortal.plane.isBehind(center)) {
				// ignore if behind the portal
				player = null;
			}
		}

		PortalPath.With<Player> throughPortals = getNearestPlayerRecursive(level, pos, radius, creative, entered, noPath);
		if (throughPortals == null) {
			noPath.add(portal);
		} else {
			Portal exitedPortal = portal.opposite().orElseThrow().get();
			Portal enteredPortal = throughPortals.path().entries.getFirst().entered().get();
			if (exitedPortal.plane.isBehind(enteredPortal.data.origin())) {
				// also ignore if behind the portal
				throughPortals = null;
			}
		}

		if (player == null) {
			return throughPortals == null ? null : throughPortals.prepend(portal, portal.opposite().orElseThrow());
		} else if (throughPortals == null) {
			return PortalPath.of(portal).with(player);
		}

		double playerDistance = player.position().distanceTo(pos);
		double distanceThroughPortals = throughPortals.path().length(pos, player.position());
		if (playerDistance <= distanceThroughPortals) {
			return PortalPath.of(portal).with(player);
		} else {
			return throughPortals.prepend(portal);
		}
	}
}
