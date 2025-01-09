package io.github.fusionflux.portalcubed.content.portal.manager.lookup.collision;

import java.util.List;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class CollisionPatch {
	public final Level level;
	public final BlockPos pos;
	public final PortalInstance portal;
	public final PortalInstance linked;

	protected CollisionPatch(Level level, BlockPos pos, PortalInstance portal, PortalInstance linked) {
		this.level = level;
		this.pos = pos;
		this.portal = portal;
		this.linked = linked;
	}

	public boolean appliesTo(Entity entity) {
		return this.portal.entityCollisionBounds.intersects(entity.getBoundingBox());
	}

	public abstract VoxelShape apply(VoxelShape shape, EntityCollisionContext ctx);

	public static class Complex extends CollisionPatch {
		private final List<BlockPos> blocks;
		private final VoxelShape modificationShape;

		protected Complex(Level level, BlockPos pos, PortalInstance portal, PortalInstance linked, List<BlockPos> blocks) {
			super(level, pos, portal, linked);
			this.blocks = blocks;
			this.modificationShape = this.portal.blockModificationShapes.get(pos);
		}

		@Override
		public VoxelShape apply(VoxelShape shape, EntityCollisionContext ctx) {
			// step 1: cut out the part of the shape that intersects with the portal's modification area
			shape = shape.move(this.pos.getX(), this.pos.getY(), this.pos.getZ());
			shape = Shapes.joinUnoptimized(shape, this.modificationShape, BooleanOp.ONLY_FIRST);
			shape = shape.move(-this.pos.getX(), -this.pos.getY(), -this.pos.getZ());
			if (true) return shape;

			Vec3 center = Vec3.atCenterOf(this.pos);
			Vec3 teleportedCenter = PortalTeleportHandler.teleportAbsoluteVecBetween(center, this.portal, this.linked);

			// step 2: overlay shapes on other side of portal onto the shape
			for (BlockPos pos : this.blocks) {
				BlockState state = this.level.getBlockState(pos);
				VoxelShape blockShape = state.getCollisionShape(this.level, pos, ctx);
				if (blockShape.isEmpty())
					continue;
				VoxelShape teleported = null;//PortalTeleportHandler.teleportRelativeShape(blockShape, this.linked, this.portal);

				Vec3 posCenter = Vec3.atCenterOf(pos);
				Vec3 offset = teleportedCenter.vectorTo(posCenter);

				VoxelShape moved = teleported.move(offset.x, offset.y, offset.z);
				shape = Shapes.or(shape, moved);
			}
			return shape;
		}
	}
}
