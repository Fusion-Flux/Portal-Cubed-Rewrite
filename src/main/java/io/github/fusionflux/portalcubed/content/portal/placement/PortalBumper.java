package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2dc;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalBumper {
	public static final int SURFACE_SEARCH_RADIUS = 2;
	public static final double MAX_BUMP_DISTANCE = 1.25;  //1.5 is closer to Portal's max bump distance, but 1.25 works a bit better with weird Minecraft geometry
	public static final int MAX_BUMPS = 5;

	// I would love to put the debug rendering on f3+p but this is server-side code, and I'm not adding a packet for it
	public static final boolean EVIL_DEBUG_RENDERING = true;
	public static final boolean DEBUG_SURFACE = false;

	@Nullable
	public static PortalPlacement findValidPlacement(@Nullable PortalId ignored, ServerLevel level, Vec3 initial, float yRot, BlockPos surfacePos, Direction face, @Nullable Angle bias, @Nullable Angle forcedRotation) {
		Collection<PortalableSurface> surfaceCandidates = getSurfaceCandidates(ignored, level, initial, surfacePos, face);
		if (surfaceCandidates.isEmpty())
			return null;

		Angle rotation = forcedRotation != null ? forcedRotation : PortalData.normalToFlatRotation(face, yRot);
		boolean bumpThroughWalls = level.getGameRules().getBoolean(PortalCubedGameRules.PORTALS_BUMP_THROUGH_WALLS);

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
				Vector3f up = surface.rotation().transform(new Vector3f(0, 0, 1));
				Line upLine = new Line(surface.origin(), surface.origin().add(up.x, up.y, up.z));
				DebugRendering.addLine(100, upLine, Color.CYAN);
			}

			List<PortalCandidate> candidates = new ArrayList<>();
			for (PortalCandidate portal : getInitialCandidates(level, surface, rotation, forcedRotation != null)) {
				if (EVIL_DEBUG_RENDERING) {
					for (Line2d portalSide : portal.lines()) {
						DebugRendering.addLine(100, portalSide.to3d(surface), Color.GREEN);
					}
				}

				findCandidates(surface, portal, bumpThroughWalls, candidates::add);
			}

			if (candidates.isEmpty())
				continue;

			// choose nearest found location
			if (candidates.size() > 1) {
				candidates.sort(getCandidateComparator(rotation, bias));
			}

			PortalCandidate finalLocation = candidates.getFirst();

			if (EVIL_DEBUG_RENDERING) {
				for (Line2d portalSide : finalLocation.lines()) {
					Color color = portalSide == finalLocation.top() ? Color.PURPLE : Color.YELLOW;
					DebugRendering.addLine(100,  portalSide.to3d(surface), color);
				}
			}

			Vec3 finalPos = surface.to3d(finalLocation.center());
			// no idea why this needs to be negative
			Quaternionf portalRotation = surface.rotation().rotateY(-finalLocation.rot().radF(), new Quaternionf());
			return new PortalPlacement(finalPos, portalRotation, finalLocation.rot());
		}

		return null;
	}

	private static Collection<PortalCandidate> getInitialCandidates(ServerLevel level, PortalableSurface surface, Angle rotation, boolean forced) {
		if (forced) {
			return List.of(PortalCandidate.initial(rotation));
		}

		if (!surface.supportsPortalRotation() && !level.getGameRules().getBoolean(PortalCubedGameRules.ALLOW_ROTATED_WALL_PORTALS)) {
			return List.of(PortalCandidate.initial(Angle.R0));
		}

		// don't duplicate the rotation if it's already a 90 degree increment
		Set<Angle> angles = new HashSet<>(Angle.INCREMENTS);
		angles.add(rotation);

		return angles.stream().map(PortalCandidate::initial).toList();
	}

	private static Comparator<PortalCandidate> getCandidateComparator(Angle desiredAngle, @Nullable Angle bias) {
		Comparator<PortalCandidate> byAngle = Comparator.comparingDouble(candidate -> candidate.rot().distanceTo(desiredAngle));
		Comparator<PortalCandidate> byDistance = Comparator.comparingDouble(candidate -> candidate.center().length());
		// prefer angle most, then distance
		Comparator<PortalCandidate> chained = byAngle.thenComparing(byDistance);
		return bias == null ? chained : chained.thenComparing(candidate -> candidate.rot().distanceTo(bias));
	}

	private static void findCandidates(PortalableSurface surface, PortalCandidate first, boolean bumpThroughWalls, Consumer<PortalCandidate> output) {
		Deque<PortalCandidate> queue = new ArrayDeque<>();
		queue.add(first);
		// track every tested candidate to avoid duplicating work
		List<PortalCandidate> all = new ArrayList<>(queue);

		while (!queue.isEmpty()) {
			PortalCandidate candidate = queue.removeFirst();
			boolean hit = false;

			walls: for (Line2d wall : surface.walls()) {
				Vector2d offset = collide(candidate, wall);
				if (offset == null)
					continue;

				hit = true;

				if (candidate.bumps() > MAX_BUMPS)
					continue;

				PortalCandidate moved = candidate.bumped(offset);

				if (moved.center().distance(first.center()) > MAX_BUMP_DISTANCE)
					continue;

				// if this new candidate is unique, add it to the queue
				for (PortalCandidate seen : all) {
					if (seen.center().distance(moved.center()) <= 1e-3) {
						continue walls;
					}
				}

				all.add(moved);
				queue.add(moved);

				if (EVIL_DEBUG_RENDERING) {
					for (Line2d line : moved.lines()) {
						DebugRendering.addLine(10, line.to3d(surface), Color.RED);
					}
				}
			}

			if (hit) {
				continue;
			}

			// no walls have been hit, do postprocess validation

			if (!bumpThroughWalls) {
				Line2d path = new Line2d(first.center(), candidate.center());
				if (surface.intersectsCollision(path)) {
					continue; // went through a wall
				}
			} else if (!surface.areOnSameSideOfBounds(first.center(), candidate.center())) {
				// bumping through walls is allowed, but we still need to make sure the result is in bounds
				continue;
			}

			// valid candidate found
			output.accept(candidate);
		}
	}

	private static Collection<PortalableSurface> getSurfaceCandidates(@Nullable PortalId ignored, ServerLevel level, Vec3 initial, BlockPos surfacePos, Direction face) {
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

		PortalableSurface flat = getFlatSurface(ignored, level, initial, surfacePos, face);
		if (flat != null && !flat.walls().isEmpty()) {
			surfaces.add(flat);
		}

		return surfaces;
	}

	// 0, 0 in surface coords is where the portal starts

	@Nullable
	private static PortalableSurface getFlatSurface(@Nullable PortalId ignored, ServerLevel level, Vec3 initial, BlockPos pos, Direction face) {
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

		for (BlockPos surfacePos : BlockPos.spiralAround(pos, SURFACE_SEARCH_RADIUS, right, up)) {
			if (!collectColumn(level, initial, surfacePos, face, walls) && pos.equals(surfacePos)) {
				// if the initially hit block has no portalable surface, fully cancel
				return null;
			}
		}

		cancelOutOpposites(walls);

		PortalableSurface surface = new PortalableSurface(surfaceRotation, initial, walls, face.getAxis() == Direction.Axis.Y);

		findOtherPortals(ignored, level, surface, initial, pos, face, up, right, walls);

		return surface;
	}

	private static boolean collectColumn(ServerLevel level, Vec3 initial, BlockPos pos, Direction face, List<Line2d> walls) {
		BlockState surface = level.getBlockState(pos);
		VoxelShape shape = getPortalVisibleShape(surface, level, pos);
		if (shape.isEmpty())
			return false;

		BlockPos inFrontPos = pos.relative(face);
		BlockState inFront = level.getBlockState(inFrontPos);
		boolean overridePortalability = overridesPortalability(inFront, face);
		boolean portalable = overridePortalability ? isPortalable(level, inFront) : isPortalable(level, surface);
		if (!portalable)
			return false;

		VoxelShape frontShape = overridePortalability ? Shapes.empty() : getPortalVisibleShape(inFront, level, inFrontPos);

		// check behind surface to allow poking through (ex. pedestal button under carpet)
		BlockPos behindPos = pos.relative(face, -1);
		BlockState behind = level.getBlockState(behindPos);
		VoxelShape behindShape = getPortalVisibleShape(behind, level, behindPos);

		processShape(shape, pos, initial, face, walls, true);
		processShape(behindShape, behindPos, initial, face, walls, false);
		processShape(frontShape, inFrontPos, initial, face, walls, false);

		return true;
	}

	private static void processShape(VoxelShape shape, BlockPos pos, Vec3 initial, Direction face, List<Line2d> walls, boolean include) {
		if (shape.isEmpty())
			return;

		Direction.Axis axis = face.getAxis();
		double surfaceOnAxis = initial.get(axis);

		for (AABB box : shape.toAabbs()) {
			AABB absolute = box.move(pos);

			double min = absolute.min(axis);
			double max = absolute.max(axis);

			// skip boxes that do not intersect the surface
			// curse you floating point
			if (surfaceOnAxis < (min - 1e-5) || surfaceOnAxis > (max + 1e-5))
				continue;

			double expected = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? max : min;
			// if surfaceOnAxis != expected, this box is not a part of the surface, but is instead a bounding wall of it
			boolean includeBox = include && Mth.equal(surfaceOnAxis, expected);

			Vec3 centerOnAxis = absolute.getCenter().with(axis, surfaceOnAxis);
			Vec3 offset = initial.vectorTo(centerOnAxis);
			AABB centered = box.move(box.getCenter().scale(-1));
			AABB relative = centered.move(offset);
			getWallsFromBox(relative, face, includeBox, walls::add);
		}
	}

	private static void findOtherPortals(@Nullable PortalId ignored, ServerLevel level, PortalableSurface surface, Vec3 initial, BlockPos pos, Direction face, Direction up, Direction right, List<Line2d> walls) {
		BlockPos inFront = pos.relative(face);
		BlockPos max = inFront.relative(up, SURFACE_SEARCH_RADIUS).relative(right, SURFACE_SEARCH_RADIUS);
		BlockPos min = pos.relative(up, -SURFACE_SEARCH_RADIUS).relative(right, -SURFACE_SEARCH_RADIUS);
		AABB area = AABB.encapsulatingFullBlocks(min, max);

		level.portalManager().lookup().getPortals(area).forEach(holder -> {
			if (ignored != null && holder.matches(ignored))
				return;

			PortalInstance portal = holder.portal();
			if (!Mth.equal(face.getUnitVec3().dot(portal.normal), 1))
				return; // not facing the same way

			Direction.Axis axis = face.getAxis();
			double posOnAxis = portal.data.origin().get(axis);
			if (!Mth.equal(initial.get(axis), posOnAxis))
				return; // not on the same plane

			for (Line line : portal.visualQuad.lines()) {
				walls.add(line.to2d(surface).flip().withSource(Line2d.Source.PORTAL));
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

	private static boolean isPortalable(ServerLevel level, BlockState state) {
		if (hasLiquid(state))
			return false;

		if (level.getGameRules().getBoolean(PortalCubedGameRules.RESTRICT_VALID_PORTAL_SURFACES)) {
			return state.is(PortalCubedBlockTags.UNRESTRICTED_PORTAL_SURFACES);
		}

		return !state.is(PortalCubedBlockTags.CANT_PLACE_PORTAL_ON);
	}

	private static boolean overridesPortalability(BlockState inFront, Direction offset) {
		if (hasLiquid(inFront))
			return true;

		if (!inFront.is(PortalCubedBlockTags.OVERRIDES_PORTALABILITY))
			return false;

		// TODO: datadrive this better
		if (!(inFront.getBlock() instanceof MultifaceBlock))
			return true;

		return MultifaceBlock.hasFace(inFront, offset.getOpposite());
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

	private static VoxelShape getPortalVisibleShape(BlockState state, BlockGetter level, BlockPos pos) {
		return getPortalVisibleShape(state, level, pos, PortalCollisionContext.INSTANCE);
	}

	public static VoxelShape getPortalVisibleShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		if (state.is(PortalCubedBlockTags.NONSOLID_TO_PORTALS))
			return Shapes.empty();

		return state.is(PortalCubedBlockTags.PORTALS_USE_BASE_SHAPE)
				? state.getShape(level, pos, ctx)
				: state.getCollisionShape(level, pos, ctx);
	}

	private static boolean hasLiquid(BlockState state) {
		return !state.getFluidState().isEmpty() || (
				state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
		);
	}

	private static boolean handleContains(Line2d first, Line2d second, DoubleRange firstRange, DoubleRange secondRange, List<Line2d> walls) {
		if (!firstRange.contains(secondRange))
			return false;

		walls.remove(first);
		walls.remove(second);
		maybeAdd(walls, first.from(), second.to());
		maybeAdd(walls, second.from(), first.to());
		return true;
	}

	private static boolean handleOverlap(Line2d first, Line2d second, DoubleRange firstRange, DoubleRange secondRange, List<Line2d> walls) {
		if (!firstRange.contains(secondRange.min()))
			return false;

		walls.remove(first);
		walls.remove(second);
		maybeAdd(walls, first.from(), second.to());
		maybeAdd(walls, second.from(), first.to());
		return true;
	}

	private static void maybeAdd(List<Line2d> walls, Vector2dc from, Vector2dc to) {
		if (from.distance(to) > 1e-5) {
			walls.add(new Line2d(from, to));
		}
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
