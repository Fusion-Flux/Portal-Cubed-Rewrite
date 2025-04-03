package io.github.fusionflux.portalcubed.content.portal.placement;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.framework.render.debug.DebugRendering;
import io.github.fusionflux.portalcubed.framework.shape.flat.Line2d;
import io.github.fusionflux.portalcubed.framework.util.Color;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.Vec3;

// the funny
public final class StairSurfaceFinder {
	public static final double STEP_LENGTH = Math.sqrt((0.5 * 0.5) + (0.5 * 0.5));

	@Nullable
	static PortalableSurface find(ServerLevel level, Vec3 initial, BlockPos pos, Direction face, BlockState state) {
		StairsShape shape = state.getValue(StairBlock.SHAPE);
		if (shape != StairsShape.STRAIGHT)
			return null;

		Direction facing = state.getValue(StairBlock.FACING);
		Half half = state.getValue(StairBlock.HALF);

		Quaternionf rotation = PortalData.normalToRotation(facing.getOpposite(), 0);
		rotation.rotateX(Mth.DEG_TO_RAD * 45 * (half == Half.BOTTOM ? 1 : -1));

		if (PortalBumper.DEBUG_SURFACE) {
			return PortalBumper.getDebugSurface(level, rotation);
		}

		// the surface should lie on the plane made by the edges of the steps
		Vec3 offset = getNearestOnPlane(pos, half, rotation, initial);
		Vec3 origin = initial.add(offset);

		// walls need to be offset to be block aligned
		Direction right = facing.getClockWise();
		Vector2d wallOffset = new Vector2d(0, 0);

		List<Line2d> walls = new ArrayList<>();

		for (int x = 0; x <= 0; x++) {
			BlockPos offsetPos = pos.relative(right, x);
			Vector2d newWallOffset = wallOffset.add(-x, 0, new Vector2d());
			connectStairUpwards(level, newWallOffset, offsetPos, facing, half, 0, walls);
			connectStairDownwards(level, newWallOffset, offsetPos, facing, half, 0, walls);
		}

		PortalBumper.cancelOutOpposites(walls);
		return new PortalableSurface(rotation, origin, walls, false);
	}

	private static Vec3 getNearestOnPlane(BlockPos pos, Half half, Quaternionf rotation, Vec3 initial) {
		Vec3 edgeOfTopStep = Vec3.upFromBottomCenterOf(pos, half == Half.BOTTOM ? 1 : 0);
		// I feel like I'm going insane, why are Y and Z swapped here
		Vector3d planeNormal = rotation.transform(new Vector3d(0, 0, 1));
		Vec3 nearestOnPlane = TransformUtils.toMc(Intersectiond.findClosestPointOnPlane(
				edgeOfTopStep.x, edgeOfTopStep.z, edgeOfTopStep.y,
				planeNormal.x, planeNormal.z, planeNormal.y,
				initial.x, initial.z, initial.y,
				new Vector3d()
		));

		DebugRendering.addPos(100, nearestOnPlane, Color.ORANGE);
		System.out.println(nearestOnPlane);

		// discard components of offset that don't offset out of the stairs
		Vec3 surfaceNormal = TransformUtils.toMc(rotation.transform(new Vector3d(0, 1, 0)));
		return nearestOnPlane;
	}

	private static boolean connectStairUpwards(ServerLevel level, Vector2dc offset, BlockPos pos, Direction facing, Half half, int distance, List<Line2d> walls) {
		if (cannotConnect(level, pos, facing, half, distance))
			return false;

		double wallTop = 0;

		BlockPos next = pos.above().relative(facing);
		//DebugRendering.addBox(100, new AABB(next), Color.RED);
		Vector2d nextOffset = offset.add(0, STEP_LENGTH * 2, new Vector2d());
		if (!connectStairUpwards(level, nextOffset, next, facing, half, distance + 1, walls)) {
			// add top wall
			walls.add(new Line2d(new Vector2d(0.5, 0).add(offset), new Vector2d(-0.5, 0).add(offset)));
		} else {
			// extend walls upward
			wallTop = STEP_LENGTH;
		}

		addInternalStepWalls(wallTop, offset, walls);
		return true;
	}

	private static boolean connectStairDownwards(ServerLevel level, Vector2dc offset, BlockPos pos, Direction facing, Half half, int distance, List<Line2d> walls) {
		if (cannotConnect(level, pos, facing, half, distance))
			return false;

		double wallTop = distance == 0 ? 0 : STEP_LENGTH;

		BlockPos next = pos.below().relative(facing, -1);
		Vector2d nextOffset = offset.add(0, -STEP_LENGTH * 2, new Vector2d());
		if (!connectStairDownwards(level, nextOffset, next, facing, half, distance + 1, walls)) {
			// add bottom wall
			walls.add(new Line2d(new Vector2d(-0.5, -STEP_LENGTH).add(offset), new Vector2d(0.5, -STEP_LENGTH).add(offset)));
		}

		addInternalStepWalls(wallTop, offset, walls);
		return true;
	}

	private static void addInternalStepWalls(double wallTop, Vector2dc offset, List<Line2d> walls) {
		// right and left respectively
		walls.add(new Line2d(new Vector2d(0.5, -STEP_LENGTH).add(offset), new Vector2d(0.5, wallTop).add(offset)));
		walls.add(new Line2d(new Vector2d(-0.5, wallTop).add(offset), new Vector2d(-0.5, -STEP_LENGTH).add(offset)));
	}

	private static boolean cannotConnect(ServerLevel level, BlockPos pos, Direction facing, Half half, int distance) {
		if (distance > 2)
			return true;

		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof StairBlock))
			return true;

		if (state.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT)
			return true;

		return state.getValue(StairBlock.FACING) != facing || state.getValue(StairBlock.HALF) != half;
	}
}
