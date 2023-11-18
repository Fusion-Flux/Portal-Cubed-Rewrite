package io.github.fusionflux.portalcubed.content.portal.collision;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.extension.EntityExt;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollisionManager {
	// thickness of a wall after the other side has been cut out for cross-portal collision
	public static final double WALL_THICKNESS = 0.01;
	public static final int RECURSION_DEPTH_LIMIT = 1;

	private final PortalManager portals;
	private final Level level;
	private final Map<BlockPos, ShapePatch> patches;

	public CollisionManager(PortalManager portalManager, Level level) {
		this.portals = portalManager;
		this.level = level;
		this.patches = new HashMap<>();
	}

	// modifying a shape for a portal involves 3 parts:
	// - cut out most of the shape to make it non-solid (leave the surface the portal is on intact)
	// - add in collision from other side
	// - cut a hole directly under the portal
	public VoxelShape getPortalModifiedShape(VoxelShape original, BlockPos pos, Entity entity) {
		if (getRecursionDepth(entity) > RECURSION_DEPTH_LIMIT)
			return original;
		if (entity.getType().is(PortalCubedEntityTags.PORTAL_BLACKLIST))
			return original;
		List<Portal> portals = this.getPortalsToUse(pos, entity);
		if (portals.isEmpty())
			return original;
		incrementRecursionDepth(entity);
		// move shape to absolute coords to match found collision shapes
		VoxelShape shape = original.move(pos.getX(), pos.getY(), pos.getZ());
		// cut out most of the shape beyond the surface
		Vec3 normal = portals.get(0).normal;
		Vec3 offsetIntoWall = normal.scale(-WALL_THICKNESS);
		VoxelShape cubeAtPos = Shapes.block().move(pos.getX(), pos.getY(), pos.getZ());
		VoxelShape cutBox = cubeAtPos.move(offsetIntoWall.x, offsetIntoWall.y, offsetIntoWall.z);
		shape = Shapes.join(shape, cutBox, BooleanOp.ONLY_FIRST);
		// first combine all collisions
		List<Portal> relevantPortals = new ArrayList<>(); // portals the entity is interacting with
		for (Portal portal : portals) {
			Portal linked = portal.getLinked();
			if (linked == null)
				continue;
			if (!portal.entityCollisionArea.contains(entity.position()))
				continue; // out of collision area, don't care
			relevantPortals.add(portal);
			List<VoxelShape> shapes = VoxelShenanigans.getShapesBehindPortal(level, entity, portal, linked);
			if (shapes.isEmpty())
				continue; // all non-solid
			// combine collisions
			for (VoxelShape collisionShape : shapes) {
				shape = Shapes.or(shape, collisionShape);
			}
		}
		// crop the shape to the specific BlockPos
		shape = Shapes.join(shape, cubeAtPos, BooleanOp.AND);
		// cut portal holes
		for (Portal portal : relevantPortals) {
			shape = Shapes.join(shape, portal.hole, BooleanOp.ONLY_FIRST);
		}
		decrementRecursionDepth(entity);
		// translate shape back to relative coords, and we're done here
        return shape.move(-pos.getX(), -pos.getY(), -pos.getZ());
	}

	// in most cases, this list will have a size of 1
	// in some cases, maybe 2, both on the same surface
	// rarer, 3+
	// in weird cases, the surface of each could potentially be different.
	// when that happens, prefer nearest.
	// returned list of portals is guaranteed to all have the same normal
	private List<Portal> getPortalsToUse(BlockPos pos, Entity entity) {
		Set<Portal> portals = this.portals.getPortalsAt(pos);
		if (portals.isEmpty()) {
			return List.of();
		} else if (portals.size() == 1) {
			return List.of(portals.iterator().next());
		} else {
			Map<Vec3, List<Portal>> byNormal = new HashMap<>();
			for (Portal portal : portals) {
				byNormal.computeIfAbsent(portal.normal, $ -> new ArrayList<>()).add(portal);
			}
			if (byNormal.size() == 1) {
				for (List<Portal> list : byNormal.values()) {
					return list;
				}
				throw new IllegalStateException("This should be impossible to reach");
			} else {
				// worst case, sort by distance to entity
				Vec3 selectedNormal = null;
				double closestSqrDist = Double.MAX_VALUE;
				for (Map.Entry<Vec3, List<Portal>> entry : byNormal.entrySet()) {
					for (Portal portal : entry.getValue()) {
						double distSqr = portal.origin.distanceToSqr(entity.position());
						if (distSqr < closestSqrDist) {
							closestSqrDist = distSqr;
							selectedNormal = entry.getKey();
						}
					}
				}
				return byNormal.get(selectedNormal);
			}
		}
	}

	@ApiStatus.Internal
	public void handlePortalLink(Portal a, Portal b) {
		this.handleNewPortal(a, b);
		this.handleNewPortal(b, a);
	}

	@ApiStatus.Internal
	public void handlePortalUnlink(Portal a, Portal b) {
		this.removePortal(a);
		this.removePortal(b);
	}

	private void handleNewPortal(Portal portal, Portal linked) {
		// iterate through block positions in front of the output portal
		BlockPos.betweenClosedStream(linked.collisionCollectionArea).forEach(posInFront -> {
			BlockState state = level.getBlockState(posInFront);
			if (state.isAir())
				return; // easy skip, don't care
			// convert position in front of output, to a position behind the input
			Vec3 outputOriginToCorner = linked.origin.vectorTo(Vec3.atLowerCornerOf(posInFront));
			Vec3 teleported = PortalTeleportHandler.teleportRelativeVecBetween(outputOriginToCorner, linked, portal);
			Vec3 posBehindIn = portal.origin.add(teleported);
			BlockPos targetPos = BlockPos.containing(posBehindIn);
			Vec3 inputOriginToCorner = portal.origin.vectorTo(Vec3.atLowerCornerOf(targetPos));

			ShapePatch patch = ShapePatch.create(state, level, posInFront, inputOriginToCorner);
			if (patch == null)
				return;

			this.patches.put(targetPos, patch);
		});
	}

	private void removePortal(Portal portal) {
		BlockPos.betweenClosedStream(portal.collisionModificationBox).forEach(this.patches::remove);
	}

	private static int getRecursionDepth(Entity entity) {
		return ((EntityExt) entity).pc$getPortalCollisionRecursionDepth();
	}

	private static void setRecursionDepth(Entity entity, int depth) {
		((EntityExt) entity).pc$setPortalCollisionRecursionDepth(depth);
	}

	private static void incrementRecursionDepth(Entity entity) {
		int depth = getRecursionDepth(entity);
		setRecursionDepth(entity, depth + 1);
	}

	private static void decrementRecursionDepth(Entity entity) {
		int depth = getRecursionDepth(entity);
		setRecursionDepth(entity, depth - 1);
	}
}
