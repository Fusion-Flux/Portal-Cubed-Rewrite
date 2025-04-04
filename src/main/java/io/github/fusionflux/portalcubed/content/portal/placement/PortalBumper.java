package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3f;

import io.github.fusionflux.portalcubed.content.PortalCubedGameRules;
import io.github.fusionflux.portalcubed.content.portal.PortalData;
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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalBumper {
	// I would love to put the debug rendering on f3+p but this is server-side code, and I'm not adding a packet for it
	public static final boolean EVIL_DEBUG_RENDERING = true;
	public static final boolean DEBUG_SURFACE = false;

	@Nullable
	public static PortalPlacement findValidPlacement(ServerLevel level, Vec3 initial, float yRot, BlockPos surfacePos, Direction face) {
		Collection<PortalableSurface> surfaceCandidates = getSurfaceCandidates(level, initial, surfacePos, face);
		if (surfaceCandidates.isEmpty())
			return null;

		Angle rotation = PortalData.normalToFlatRotation(face, yRot);

		for (PortalableSurface surface : surfaceCandidates) {
			PortalCandidate portal = PortalCandidate.initial(surface.supportsPortalRotation() ? rotation : Angle.ZERO);

			if (EVIL_DEBUG_RENDERING) {
				for (Line2d portalSide : portal.lines()) {
					DebugRendering.addLine(100, portalSide.to3d(surface), Color.GREEN);
				}
				for (Line2d wall : surface.walls()) {
					DebugRendering.addLine(100, wall.to3d(surface), Color.PURPLE);

					Vector2d perpendicularAxis = wall.perpendicularCcwAxis().mul(0.25);
					Line2d perpendicularLine = new Line2d(wall.midpoint(), wall.midpoint().add(perpendicularAxis));
					DebugRendering.addLine(100, perpendicularLine.to3d(surface), Color.RED);
				}
				Vector3f normal = surface.rotation().transform(new Vector3f(0, 1, 0));
				Line normalLine = new Line(surface.origin(), surface.origin().add(normal.x, normal.y, normal.z));
				DebugRendering.addLine(100, normalLine, Color.BLUE);
			}

			List<PortalCandidate> candidates = findCandidates(surface, portal, initial);
			if (candidates.isEmpty())
				continue;

			// choose nearest found location
			candidates.sort(Comparator.comparingDouble(location -> location.center().length()));
			PortalCandidate finalLocation = candidates.getFirst();

			if (EVIL_DEBUG_RENDERING) {
				for (Line2d portalSide : finalLocation.lines()) {
					DebugRendering.addLine(100,  portalSide.to3d(surface), Color.YELLOW);
				}
			}

			Vec3 finalPos = surface.to3d(finalLocation.center());
			Quaternionf portalRotation = surface.rotation().rotateY(finalLocation.rot().radF(), new Quaternionf());
			return new PortalPlacement(finalPos, portalRotation);
		}

		return null;
	}

	private static List<PortalCandidate> findCandidates(PortalableSurface surface, PortalCandidate portal, Vec3 initial) {
		// repeatedly iterate edges of surface, finding several options
		List<PortalCandidate> found = new ArrayList<>();

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
							DebugRendering.addLine(100, edge.to3d(surface), Color.RED);
							Line2d moved = new Line2d(currentPortal.center(), currentPortal.center().add(offset, new Vector2d()));
							DebugRendering.addLine(100, moved.to3d(surface), Color.CYAN);
						}

						// when a collision happens, restart the search at the new position, since newly intersecting walls may have already been checked.
						currentPortal = currentPortal.moved(offset.x(), offset.y());
						continue moves;
					}
				}

				// no collision, valid position found
				found.add(currentPortal);
				continue attempts;
			}

			// moves exhausted, next attempt
		}

		return found;
	}

	private static Collection<PortalableSurface> getSurfaceCandidates(ServerLevel level, Vec3 initial, BlockPos surfacePos, Direction face) {
		// TODO: de-hardcode this

		List<PortalableSurface> surfaces = new ArrayList<>();

		// stairs take priority if present
		BlockState state = level.getBlockState(surfacePos);
		if (state.getBlock() instanceof StairBlock) {
			PortalableSurface stair = StairSurfaceFinder.find(level, initial, surfacePos, face, state);
			if (stair != null) {
				surfaces.add(stair);
			}
		}

		PortalableSurface flat = getFlatSurface(level, initial, surfacePos, face);
		if (flat != null) {
			surfaces.add(flat);
		}

		return surfaces;
	}

	// 0, 0 in surface coords is where the portal starts

	@Nullable
	private static PortalableSurface getFlatSurface(ServerLevel level, Vec3 initial, BlockPos pos, Direction face) {
		if (isFlatSurfaceNonPortalable(level, pos, face))
			return null;

		Quaternionf surfaceRotation = PortalData.normalToRotation(face, 0);

		if (DEBUG_SURFACE) {
			return getDebugSurface(level, surfaceRotation);
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

		Direction.Axis axis = face.getAxis();
		double surfaceOnAxis = initial.get(axis);
		List<Line2d> walls = new ArrayList<>();

		for (BlockPos surfacePos : BlockPos.spiralAround(pos, 2, right, up)) {
			if (isFlatSurfaceNonPortalable(level, surfacePos, face))
				continue;

			if (false) {
				walls.add(new Line2d(new Vector2d(-0.5, -0.5), new Vector2d(0.5, 0.5)));
				continue;
			}

			BlockState surface = level.getBlockState(surfacePos);
			// TODO: make portal holes show up with a custom context
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
					getWallsFromBox(relative, face, walls::add);
				}
			}
		}

		cancelOutOpposites(walls);

		// if (face.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
		// 	surfaceRotation.rotateZ(Mth.DEG_TO_RAD * 180);
		// }

		return new PortalableSurface(surfaceRotation, initial, walls, axis == Direction.Axis.Y);
	}

	private static void getWallsFromBox(AABB box, Direction face, Consumer<Line2d> output) {
		// abandon all hope ye who enter here.
		// this may appear to be an intuitive yet clunky solution at first glance, but do not be fooled.
		// these numbers are complete nonsense, conjured out of thin air through trial and error after literal days of attempting a better solution.
		// make no attempt to resolve this, or else you may face unforeseen consequences.
		//									  D			U		  N			S		  E			W
		double relativeMinX = choose(face, box.maxX, box.minX, box.minX, -box.maxX, box.minZ, -box.maxZ);
		double relativeMaxX = choose(face, box.minX, box.maxX, box.maxX, -box.minX, box.maxZ, -box.minZ);
		double relativeMinY = choose(face, -box.minZ, box.minZ, box.minY, box.minY, box.minY, box.minY);
		double relativeMaxY = choose(face, -box.maxZ, box.maxZ, box.maxY, box.maxY, box.maxY, box.maxY);

		output.accept(new Line2d(new Vector2d(relativeMinX, relativeMinY), new Vector2d(relativeMaxX, relativeMinY)));
		output.accept(new Line2d(new Vector2d(relativeMaxX, relativeMinY), new Vector2d(relativeMaxX, relativeMaxY)));
		output.accept(new Line2d(new Vector2d(relativeMaxX, relativeMaxY), new Vector2d(relativeMinX, relativeMaxY)));
		output.accept(new Line2d(new Vector2d(relativeMinX, relativeMaxY), new Vector2d(relativeMinX, relativeMinY)));
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

	static PortalableSurface getDebugSurface(ServerLevel level, Quaternionf surfaceRotation) {
		float h = level.getGameTime() / 100f;
		return new PortalableSurface(surfaceRotation, List.of(
				new Line2d(new Vector2d(-Math.cos(h), -Math.sin(h)), new Vector2d(Math.cos(h), Math.sin(h)))
		), true);
	}

	public static void cancelOutOpposites(List<Line2d> walls) {
		Set<Line2d> removed = new HashSet<>();
		walls.removeIf(first -> {
			if (removed.contains(first))
				return true;

			for (Line2d second : walls) {
				if (first.from().distance(second.to()) < 1e-5 && first.to().distance(second.from()) < 1e-5) {
					removed.add(second);
					return true;
				}
			}
			return false;
		});
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

			if ((boxRange.min() - lineRange.max() > 0) || (lineRange.min() - boxRange.max() > 0)) {
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
