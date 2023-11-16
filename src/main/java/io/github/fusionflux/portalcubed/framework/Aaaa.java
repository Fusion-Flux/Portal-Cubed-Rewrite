package io.github.fusionflux.portalcubed.framework;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.framework.extension.EntityCollisionContextExt;
import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Set;

public class Aaaa {
	public static VoxelShape h(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext ctx) {
		if (world instanceof Level level && ctx instanceof EntityCollisionContext entityCtx) {
			EntityCollisionContextExt ctxExt = (EntityCollisionContextExt) entityCtx;
			if (ctxExt.pc$doPortalCollision()) {
				Entity entity = entityCtx.getEntity();
				if (entity != null) {
					PortalManager manager = PortalManager.of(level);
					Set<Portal> portals = manager.getPortalsAt(pos);
					if (!portals.isEmpty()) {
						// prevent infinite loop
						ctxExt.pc$setPortalCollision(false);

						MutableObject<VoxelShape> shapeHolder = new MutableObject<>(shape);
						for (Portal portal : portals) {
							Portal linked = portal.getLinked();
							if (linked == null)
								continue;
							if (!portal.collisionArea.contains(entity.position()))
								continue;
							// iterate through blocks behind input portal
							BlockPos.betweenClosedStream(portal.blockCollisionArea).forEach(posBehindPortal -> {
								AABB blockArea = new AABB(posBehindPortal);
								// for each block, teleport the bounds to in front of the output
								AABB teleported = PortalTeleportHandler.teleportBoxBetween(blockArea, portal, linked);
								Vec3 boxCenter = teleported.getCenter();
								// iterate through blocks in the new bounds
								// collect and combine bounds
								MutableObject<VoxelShape> combined = new MutableObject<>(Shapes.empty());
								BlockPos.betweenClosedStream(teleported).forEach(posInFront -> {
									BlockState state = level.getBlockState(posInFront);
									VoxelShape collisionShape = state.getCollisionShape(level, posInFront, ctx);
									if (!collisionShape.isEmpty()) {
										Vec3 center = Vec3.atCenterOf(posInFront);
										Vec3 offset = center.vectorTo(boxCenter);
										VoxelShape moved = collisionShape.move(offset.x, offset.y, offset.z);

										VoxelShape existing = combined.getValue();
										VoxelShape merged = Shapes.or(existing, moved);
										combined.setValue(merged);
									}
								});
								VoxelShape merged = combined.getValue();
								if (!merged.isEmpty()) {
									// transform collected collision to be behind the input portal
									VoxelShape transformed = VoxelShenanigans.transformShapeAcross(merged, linked, portal);
									// limit to 1x1
									VoxelShape bounded = Shapes.join(transformed, Shapes.block(), BooleanOp.AND);
									shapeHolder.setValue(bounded);
								}
							});
						}
						// reset
						ctxExt.pc$setPortalCollision(true);

						return shapeHolder.getValue();
					}
				}
			}
		}
		return shape;
	}
}
