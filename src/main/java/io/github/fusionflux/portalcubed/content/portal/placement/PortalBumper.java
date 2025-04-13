package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.content.portal.PortalId;
import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.shape.Line;
import io.github.fusionflux.portalcubed.framework.shape.flat.Line2d;
import io.github.fusionflux.portalcubed.framework.util.Angle;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.DoubleRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalBumper {
	// I would love to put the debug rendering on f3+p but this is server-side code, and I'm not adding a packet for it
	public static final boolean EVIL_DEBUG_RENDERING = true;
	public static final boolean DEBUG_SURFACE = false;

	@Nullable
	public static PortalPlacement findValidPlacement(PortalId id, ServerLevel level, Vec3 initial, float yRot, BlockPos surfacePos, Direction face) {
		Collection<PortalableSurface> surfaceCandidates = getSurfaceCandidates(id, level, initial, surfacePos, face);
		if (surfaceCandidates.isEmpty())
			return null;

		// no I don't know why this needs to be negative.
		// yes it's cancelled out by the other random negative below.
		// but it's not cancelled out in other places, and that's required for correct behavior.
		Angle rotation = PortalData.normalToFlatRotation(face, -yRot);

		for (PortalableSurface surface : surfaceCandidates) {
			if (EVIL_DEBUG_RENDERING) {
				for (Line2d wall : surface.walls()) {
					Line line = wall.to3d(surface);
					DebugRendering.addLine(100, line, Color.PURPLE);
					// DebugRendering.addPos(100, line.from(), Color.PURPLE);
					// DebugRendering.addPos(100, line.to(), Color.PURPLE);

					Vector2d perpendicularAxis = wall.perpendicularCcwAxis().mul(0.25);
					Line2d perpendicularLine = new Line2d(wall.midpoint(), wall.midpoint().add(perpendicularAxis));
					DebugRendering.addLine(100, perpendicularLine.to3d(surface), Color.RED);
				}
				Vector3f normal = surface.rotation().transform(new Vector3f(0, 1, 0));
				Line normalLine = new Line(surface.origin(), surface.origin().add(normal.x, normal.y, normal.z));
				DebugRendering.addLine(100, normalLine, Color.BLUE);
			}

			List<PortalCandidate> candidates = new ArrayList<>();
			for (PortalCandidate portal : getInitialCandidates(surface, rotation)) {
				if (EVIL_DEBUG_RENDERING) {
					for (Line2d portalSide : portal.lines()) {
						DebugRendering.addLine(100, portalSide.to3d(surface), Color.GREEN);
					}
				}

				findCandidates(surface, portal, initial, candidates::add);
			}

			if (candidates.isEmpty())
				continue;

			// choose nearest found location
			candidates.sort(getCandidateComparator(rotation));
			PortalCandidate finalLocation = candidates.getFirst();

			if (EVIL_DEBUG_RENDERING) {
				for (Line2d portalSide : finalLocation.lines()) {
					DebugRendering.addLine(100,  portalSide.to3d(surface), Color.YELLOW);
				}
			}

			Vec3 finalPos = surface.to3d(finalLocation.center());
			Quaternionf portalRotation = surface.rotation().rotateY(-finalLocation.rot().radF(), new Quaternionf());
			return new PortalPlacement(finalPos, portalRotation);
		}

		return null;
	}

	private static List<PortalCandidate> getInitialCandidates(PortalableSurface surface, Angle rotation) {
		if (!surface.supportsPortalRotation()) {
			return List.of(PortalCandidate.initial(Angle.R0));
		}

		List<PortalCandidate> list = new ArrayList<>();
		list.add(PortalCandidate.initial(rotation));
		// also test 90 degree increments, to handle cases like floor and ceiling 1x2s where space is very limited
		list.add(PortalCandidate.initial(Angle.R0));
		list.add(PortalCandidate.initial(Angle.R90));
		list.add(PortalCandidate.initial(Angle.R180));
		list.add(PortalCandidate.initial(Angle.R270));
		return list;
	}

	private static Comparator<PortalCandidate> getCandidateComparator(Angle desiredAngle) {
		// prefer angle most, then distance
		Comparator<PortalCandidate> byAngle = Comparator.comparingDouble(candidate -> candidate.rot().distanceTo(desiredAngle));
		Comparator<PortalCandidate> byDistance = Comparator.comparingDouble(candidate -> candidate.center().length());
		return byAngle.thenComparing(byDistance);
	}

	private static void findCandidates(PortalableSurface surface, PortalCandidate portal, Vec3 initial, Consumer<PortalCandidate> output) {
		// repeatedly iterate edges of surface, finding several options

		// make a mutable copy for shuffling
		List<Line2d> walls = new ArrayList<>(surface.walls());
		// make shots deterministic for the exact same position
		Random random = new Random(initial.hashCode());

		// limit attempts to not run forever
		attempts: for (int attempt = 0; attempt < 5; attempt++) {
			PortalCandidate currentPortal = portal;
			// a different order is used each try so each search is unique and more than one candidate can be found.
			Collections.shuffle(walls, random);

			// limit moves, after a bunch it's probably cycling
			moves: for (int movement = 0; movement < 5; movement++) {
				for (Line2d edge : walls) {
					Vector2d offset = collide(currentPortal, edge);
					if (offset != null) {
						if (EVIL_DEBUG_RENDERING) {
							//DebugRendering.addLine(100, edge.to3d(surface), Color.RED);
							Line2d moved = new Line2d(currentPortal.center(), currentPortal.center().add(offset, new Vector2d()));
							DebugRendering.addLine(100, moved.to3d(surface), Color.CYAN);
						}

						// when a collision happens, restart the search at the new position, since newly intersecting walls may have already been checked.
						currentPortal = currentPortal.moved(offset.x(), offset.y());
						continue moves;
					}
				}

				// no collision, valid position found
				// make sure the position is actually within the bounds though
				if (surface.contains(currentPortal.center())) {
					output.accept(currentPortal);
				}

				continue attempts;
			}

			// moves exhausted, next attempt
		}
	}

	private static Collection<PortalableSurface> getSurfaceCandidates(PortalId id, ServerLevel level, Vec3 initial, BlockPos surfacePos, Direction face) {
		// TODO: de-hardcode this

		List<PortalableSurface> surfaces = new ArrayList<>();

		// stairs take priority if present
		// BlockState state = level.getBlockState(surfacePos);
		// if (state.getBlock() instanceof StairBlock) {
		// 	PortalableSurface stair = StairSurfaceFinder.find(id, level, initial, surfacePos, face, state);
		// 	if (stair != null) {
		// 		surfaces.add(stair);
		// 	}
		// }

		PortalableSurface flat = getFlatSurface(id, level, initial, surfacePos, face);
		if (flat != null) {
			surfaces.add(flat);
		}

		return surfaces;
	}

	// 0, 0 in surface coords is where the portal starts

	@Nullable
	private static PortalableSurface getFlatSurface(PortalId id, ServerLevel level, Vec3 initial, BlockPos pos, Direction face) {
		if (isFlatSurfaceNonPortalable(level, pos, face))
			return null;

		Quaternionf surfaceRotation = PortalData.normalToRotation(face, 0);

		if (DEBUG_SURFACE) {
			return getDebugSurface(level, initial, surfaceRotation);
		}

		Direction up = switch (face) {
			case UP -> Direction.NORTH;
			case DOWN -> Direction.SOUTH;
			default -> Direction.UP;
		};
		Direction right = switch (face) {
			case UP -> Direction.EAST;
			case DOWN -> Direction.WEST;
			default -> face.getCounterClockWise();
		};

		List<Line2d> walls = new ArrayList<>();

		collectSurface(level, initial, pos, right, up, face, walls, true);
		collectSurface(level, initial, pos.relative(face), right, up, face, walls, false);

		PortalableSurface surface = new PortalableSurface(surfaceRotation, initial, walls, face.getAxis() == Direction.Axis.Y);

		findOtherPortals(id, level, surface, initial, pos, face, up, right, walls);
		cancelOutOpposites(walls);

		return surface;
	}

	private static void collectSurface(ServerLevel level, Vec3 initial, BlockPos pos, Direction right, Direction up, Direction face, List<Line2d> walls, boolean include) {
		Direction.Axis axis = face.getAxis();
		double surfaceOnAxis = initial.get(axis);

		for (BlockPos surfacePos : BlockPos.spiralAround(pos, 2, right, up)) {
			if (include && isFlatSurfaceNonPortalable(level, surfacePos, face))
				continue;

			BlockState surface = level.getBlockState(surfacePos);
			VoxelShape shape = surface.getCollisionShape(level, surfacePos);
			for (AABB box : shape.toAabbs()) {
				AABB absolute = box.move(surfacePos);
				double min = absolute.min(axis);
				double max = absolute.max(axis);
				if (Mth.equal(min, surfaceOnAxis) || Mth.equal(max, surfaceOnAxis)) {
					Vec3 centerOnAxis = absolute.getCenter().with(axis, surfaceOnAxis);
					Vec3 offset = initial.vectorTo(centerOnAxis);
					AABB centered = box.move(box.getCenter().scale(-1));
					AABB relative = centered.move(offset);
					getWallsFromBox(relative, face, include, walls::add);
				}
			}
		}
	}

	private static void findOtherPortals(PortalId placing, ServerLevel level, PortalableSurface surface, Vec3 initial, BlockPos pos, Direction face, Direction up, Direction right, List<Line2d> walls) {
		BlockPos inFront = pos.relative(face);
		BlockPos max = inFront.relative(up, 2).relative(right, 2);
		BlockPos min = pos.relative(up, -2).relative(right, -2);
		AABB area = AABB.encapsulatingFullBlocks(min, max);

		level.portalManager().forEachPortalInBox(area, holder -> {
			if (holder.id().equals(placing))
				return;

			PortalInstance portal = holder.portal();
			if (!Mth.equal(face.getUnitVec3().dot(portal.normal), 1))
				return; // not facing the same way

			Direction.Axis axis = face.getAxis();
			double posOnAxis = portal.data.origin().get(axis);
			if (!Mth.equal(initial.get(axis), posOnAxis))
				return; // not on the same plane

			Vector2d origin = surface.to2d(portal.data.origin());

			Vector3d absRight = new Vector3d(-1, 0, 0);
			Vector3d relativeRight = portal.rotation().transform(absRight, new Vector3d());

			Angle angle = Angle.ofRad(relativeRight.angleSigned(
					absRight.x, absRight.y, absRight.z,
					portal.normal.x, portal.normal.y, portal.normal.z
			));

			for (Line2d line : PortalCandidate.other(origin, angle).lines()) {
				walls.add(line.flip());
			}
		});
	}

	private static void getWallsFromBox(AABB box, Direction face, boolean include, Consumer<Line2d> output) {
		// abandon all hope ye who enter here.
		// this may appear to be an intuitive yet clunky solution at first glance, but do not be fooled.
		// these numbers are complete nonsense, conjured out of thin air through trial and error after literal days of attempting a better solution.
		// make no attempt to resolve this, or else you may face unforeseen consequences.
		//									  D			U		  N			S		  E			W
		double relativeMinX = choose(face, box.maxX, box.minX, box.minX, -box.maxX, box.minZ, -box.maxZ);
		double relativeMaxX = choose(face, box.minX, box.maxX, box.maxX, -box.minX, box.maxZ, -box.minZ);
		double relativeMinY = choose(face, -box.minZ, box.minZ, box.minY, box.minY, box.minY, box.minY);
		double relativeMaxY = choose(face, -box.maxZ, box.maxZ, box.maxY, box.maxY, box.maxY, box.maxY);

		output.accept(boxLine(relativeMinX, relativeMinY, relativeMaxX, relativeMinY, include));
		output.accept(boxLine(relativeMaxX, relativeMinY, relativeMaxX, relativeMaxY, include));
		output.accept(boxLine(relativeMaxX, relativeMaxY, relativeMinX, relativeMaxY, include));
		output.accept(boxLine(relativeMinX, relativeMaxY, relativeMinX, relativeMinY, include));
	}

	private static Line2d boxLine(double fromX, double fromY, double toX, double toY, boolean include) {
		Vector2d from = new Vector2d(fromX, fromY);
		Vector2d to = new Vector2d(toX, toY);
		return new Line2d(include ? from : to, include ? to : from);
	}

	private static double choose(Direction direction, double down, double up, double north, double south, double east, double west) {
		return switch (direction) {
			case DOWN -> down;
			case UP -> up;
			case NORTH -> north;
			case SOUTH -> south;
			case EAST -> east;
			case WEST -> west;
		};
	}

	private static boolean isFlatSurfaceNonPortalable(ServerLevel level, BlockPos pos, Direction face) {
		BlockState state = level.getBlockState(pos);
		BlockState inFront = level.getBlockState(pos.relative(face));

		if (level.getGameRules().getBoolean(PortalCubedGameRules.RESTRICT_VALID_PORTAL_SURFACES)) {
			if (inFront.is(PortalCubedBlockTags.ADDS_PORTALABILITY))
				return false;

			if (state.is(PortalCubedBlockTags.UNRESTRICTED_PORTAL_SURFACES)) {
				return inFront.is(PortalCubedBlockTags.REMOVES_PORTALABILITY);
			}

			return true;
		}

		if (state.is(PortalCubedBlockTags.CANT_PLACE_PORTAL_ON) && !inFront.is(PortalCubedBlockTags.ADDS_PORTALABILITY))
			return true;

		return inFront.is(PortalCubedBlockTags.REMOVES_PORTALABILITY);
	}

	static PortalableSurface getDebugSurface(ServerLevel level, Vec3 initial, Quaternionf surfaceRotation) {
		List<Line2d> walls = new ArrayList<>();

		// rotat e
		// float h = level.getGameTime() / 100f;
		// walls.add(new Line2d(new Vector2d(-Math.cos(h), -Math.sin(h)), new Vector2d(Math.cos(h), Math.sin(h))));

		// opposite containment test
		walls.add(new Line2d(new Vector2d(0, 1), new Vector2d(1, 1)));
		walls.add(new Line2d(new Vector2d(0.75, 1), new Vector2d(0.25, 1)));

		// opposite intersection test
		walls.add(new Line2d(new Vector2d(0, 0), new Vector2d(1, 0)));
		walls.add(new Line2d(new Vector2d(1.5, 0), new Vector2d(0.5, 0)));

		cancelOutOpposites(walls);

		return new PortalableSurface(surfaceRotation, initial, walls, true);
	}

	public static void cancelOutOpposites(List<Line2d> walls) {
		outer: while (true) {
			for (Line2d first : walls) {
				for (Line2d second : walls) {
					// must be facing opposite directions
					Vector2d axis = first.axis();
					if (!Mth.equal(axis.dot(second.axis()), -1))
						continue;

					if (!first.isAlignedWith(second))
						continue;

					/*
					seven cases:
					1. no intersection
					2. first contains second	-| equivalent, swap first and second
					3. second contains first	-/
					4. overlap on left, first left of second	 -| equivalent, swap first and second  -| also equivalent, just side is flipped.
					5. overlap on left, second left of first  	 -/										|  -| also equivalent, just side is flipped.
					6. overlap on right, first right of second  -| equivalent, swap first and second	|  -/
					7. overlap on right, second right of first  -/								       -/
					so there's actually only 3 simplified cases.
					- no intersection
					- contains
					- overlap
					 */

					// #1: check if there's an intersection at all
					DoubleRange firstRange = first.project(axis);
					DoubleRange secondRange = second.project(axis);
					if (!firstRange.intersects(secondRange))
						continue;

					if (handleContains(first, second, firstRange, secondRange, walls) // #2
							|| handleContains(second, first, secondRange, firstRange, walls) // #3
							|| handleOverlap(first, second, firstRange, secondRange, walls) // #4 / #7
							|| handleOverlap(second, first, secondRange, firstRange, walls)) { // #5 / #6
						continue outer;
					}
				}
			}

			// when the inner loops don't end early, all opposites have been canceled.
			break;
		}
	}

	private static boolean handleContains(Line2d first, Line2d second, DoubleRange firstRange, DoubleRange secondRange, List<Line2d> walls) {
		if (!firstRange.contains(secondRange))
			return false;

		walls.remove(first);
		walls.remove(second);
		walls.add(new Line2d(first.from(), second.to()));
		walls.add(new Line2d(second.from(), first.to()));
		return true;
	}

	private static boolean handleOverlap(Line2d first, Line2d second, DoubleRange firstRange, DoubleRange secondRange, List<Line2d> walls) {
		if (!firstRange.contains(secondRange.min()))
			return false;

		walls.remove(first);
		walls.remove(second);
		walls.add(new Line2d(first.from(), second.to()));
		walls.add(new Line2d(second.from(), first.to()));
		return true;
	}

	/**
	 * @return the necessary offset to separate the portal and line, if present
	 */
	@Nullable
	private static Vector2d collide(PortalCandidate portal, Line2d line) {
		// use SAT to first determine if the portal and line are aligned using the portal's two normal axes
		Vector2d offset = sat(portal, line, List.of(portal.right().perpendicularCcwAxis(), portal.top().perpendicularCcwAxis()));
		if (offset == null)
			return null;

		// now use SAT on the line's normal only to get a final value
		return sat(portal, line, List.of(line.perpendicularCcwAxis()));
	}

	@Nullable
	private static Vector2d sat(PortalCandidate portal, Line2d line, List<Vector2dc> axes) {
		// based on https://www.sevenson.com.au/programming/sat/
		double smallestDistanceOnAxis = Double.MAX_VALUE;
		Vector2dc smallestDistanceAxis = null;

		for (Vector2dc axis : axes) {
			DoubleRange boxRange = portal.project(axis);
			DoubleRange lineRange = line.project(axis);

			if (!boxRange.intersects(lineRange)) {
				// gap found, give up
				return null;
			}

			double overlap = -(lineRange.max() - boxRange.min());

			if (overlap < smallestDistanceOnAxis) {
				smallestDistanceOnAxis = overlap;
				smallestDistanceAxis = axis;
			}
		}

		if (smallestDistanceOnAxis >= 0)
			return null;

		Objects.requireNonNull(smallestDistanceAxis);

		// this tiny extra offset avoids intersections that practically shouldn't happen but do because of float precision
		double extraOffsetScale = 1.001;
		return smallestDistanceAxis.mul(-smallestDistanceOnAxis * extraOffsetScale, new Vector2d());
	}

}
