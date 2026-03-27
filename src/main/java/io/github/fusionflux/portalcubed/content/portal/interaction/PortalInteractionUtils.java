package io.github.fusionflux.portalcubed.content.portal.interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class PortalInteractionUtils {
	private PortalInteractionUtils() {}

	/// @see #findPathLengthSqr(Level, Vec3, ToDoubleFunction, double)
	public static OptionalDouble findPathLengthSqr(Level level, Vec3 start, Vec3 end, double range) {
		return findPathLengthSqr(level, start, end::distanceTo, range);
	}

	/// [find a path through portals][#findPath(Level, Vec3, ToDoubleFunction, double, boolean)], and then get its length if found.
	public static OptionalDouble findPathLengthSqr(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range) {
		PortalPath path = findPath(level, start, distanceFunction, range, true);
		return path == null ? OptionalDouble.empty() : OptionalDouble.of(path.distanceThroughSqr(start, distanceFunction));
	}

	/// @see #findPath(Level, Vec3, ToDoubleFunction, double, boolean)
	@Nullable
	public static PortalPath findPath(Level level, Vec3 start, Vec3 end, double range) {
		return findPath(level, start, end::distanceTo, range, false);
	}

	/// @see #findPath(Level, Vec3, ToDoubleFunction, double, boolean)
	@Nullable
	public static PortalPath findPath(Level level, Vec3 start, Vec3 end, double range, boolean lineOfSight) {
		return findPath(level, start, end::distanceTo, range, lineOfSight);
	}

	/// Find a path from a start point to a target that passes through portals.
	/// @param distanceFunction a function providing the distance from a given point to the target
	/// @param range the maximum length of a found path
	/// @param lineOfSight if true, portals must be facing each other for a path to be found between them
	@Nullable
	public static PortalPath findPath(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range, boolean lineOfSight) {
		return findPathRecursive(level, start, distanceFunction, range, lineOfSight, new HashSet<>(), new HashSet<>());
	}

	@Nullable
	public static PortalPath findPathRecursive(Level level, Vec3 start, ToDoubleFunction<Vec3> distanceFunction, double range, boolean lineOfSight, Set<String> seenPairs, Set<PortalReference> noPath) {
		PortalPath shortest = null;
		double shortestDistance = Double.MAX_VALUE;

		for (PortalReference portal : level.portalManager().lookup().getPortalsAround(start, range)) {
			if ((lineOfSight && portal.get().plane.isBehind(start)) || noPath.contains(portal))
				continue;

			Optional<PortalReference> maybeOpposite = portal.opposite();
			if (maybeOpposite.isEmpty() || !seenPairs.add(portal.id.key()))
				continue;

			double distanceToPortal = start.distanceTo(portal.get().origin());
			double remainingRange = range - distanceToPortal;
			if (remainingRange > 0) {
				PortalReference linked = maybeOpposite.get();
				Vec3 newPos = linked.get().origin();
				double directDistance = distanceToPortal + distanceFunction.applyAsDouble(newPos);

				PortalPath throughPortals = findPathRecursive(level, newPos, distanceFunction, remainingRange, lineOfSight, seenPairs, noPath);
				double distThroughPortals = throughPortals == null ? Double.MAX_VALUE : throughPortals.distanceThrough(newPos, distanceFunction);
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

		return shortestDistance <= range ? shortest : null;
	}

	/// Portal-aware variant of [getEntities][Level#getEntities(EntityTypeTest, AABB, Predicate)].
	/// Does not find entities that **aren't** through portals; both methods must be used.
	/// @return a new, mutable Set of found entities
	public static <T extends Entity> Set<T> getEntities(Level level, EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> predicate) {
		Set<T> found = new HashSet<>();
		getEntitiesRecursive(level, test, area, predicate, found::add, new HashSet<>());
		return found;
	}

	/// Portal-aware variant of [Level#getEntitiesOfClass(Class, AABB)].
	/// @see #getEntities(Level, EntityTypeTest, AABB, Predicate)
	public static <T extends Entity> Set<T> getEntitiesOfClass(Level level, Class<T> clazz, AABB area) {
		return getEntities(level, EntityTypeTest.forClass(clazz), area, EntitySelector.NO_SPECTATORS);
	}

	private static <T extends Entity> void getEntitiesRecursive(Level level, EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> predicate, Consumer<T> output, Set<PortalReference> entered) {
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

				getEntitiesRecursive(level, test, transformedArea.encompassingAabb, predicate, output, entered);
				entered.remove(portal);
			});
		}
	}

	/// Portal-aware variant of [getNearestPlayer][Level#getNearestPlayer(double, double, double, double, boolean)].
	/// Does not find players that **aren't** through portals; both methods must be used.
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
				double distance = found.path().distanceThrough(start, found.value().position());
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
			Portal exitedPortal = portal.oppositeOrThrow().get();
			if (exitedPortal.plane.isBehind(center)) {
				// ignore if behind the portal
				player = null;
			}
		}

		PortalPath.With<Player> throughPortals = getNearestPlayerRecursive(level, pos, radius, creative, entered, noPath);
		if (throughPortals == null) {
			noPath.add(portal);
		} else {
			Portal exitedPortal = portal.oppositeOrThrow().get();
			Portal enteredPortal = throughPortals.path().first().reference().get();
			if (exitedPortal.plane.isBehind(enteredPortal.origin())) {
				// also ignore if behind the portal
				throughPortals = null;
			}
		}

		if (player == null) {
			return throughPortals == null ? null : throughPortals.prepend(portal, portal.oppositeOrThrow());
		} else if (throughPortals == null) {
			return PortalPath.of(portal).with(player);
		}

		double playerDistance = player.position().distanceTo(pos);
		double distanceThroughPortals = throughPortals.path().distanceThrough(pos, player.position());
		if (playerDistance <= distanceThroughPortals) {
			return PortalPath.of(portal).with(player);
		} else {
			return throughPortals.prepend(portal);
		}
	}

	/// Convert the given [HitResult] int an equivalent [miss][HitResult.Type#MISS].
	/// @param direction the direction of the raycast that produced the result, does not need to be normalized
	public static BlockHitResult convertToMiss(HitResult original, Vec3 direction) {
		Vec3 pos = original.getLocation();
		Direction face = Direction.getApproximateNearest(direction).getOpposite();
		return BlockHitResult.miss(pos, face, BlockPos.containing(pos));
	}

	/// Get all entities that are intersecting the given portal.
	public static List<Entity> getEntitiesIntersectingPortal(EntityGetter level, @Nullable Entity except, OBB area, Portal portal, Predicate<Entity> filter) {
		List<Entity> entities = level.getEntities(except, area.encompassingAabb, filter);
		if (entities.isEmpty())
			return entities;

		List<Entity> intersecting = new ArrayList<>();

		for (Entity entity : entities) {
			AABB bounds = entity.getBoundingBox();
			if (area.intersects(bounds) && portal.quad.intersects(bounds)) {
				intersecting.add(entity);
			}
		}

		return intersecting;
	}
}
