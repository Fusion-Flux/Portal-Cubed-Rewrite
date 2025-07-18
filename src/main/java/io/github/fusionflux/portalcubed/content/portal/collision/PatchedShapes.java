package io.github.fusionflux.portalcubed.content.portal.collision;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.transform.SinglePortalTransform;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import io.github.fusionflux.portalcubed.framework.shape.Quad;
import io.github.fusionflux.portalcubed.framework.shape.voxel.VoxelShenanigans;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// the shape goes into a shape press that presses the shape into a pressed shape
public final class PatchedShapes {
	// just needs to be something big enough to not impact gameplay.
	// could even be MAX_VALUE, if it didn't cause problems when math happens.
	public static final int HOLE_DEPTH = 1_000;

	private final OBB hole;
	private final Map<BlockPos, VoxelShape> holes;
	private final Map<BlockPos, VoxelShape> cache;

	public PatchedShapes(Quad quad) {
		this.hole = OBB.extrudeQuad(quad, -HOLE_DEPTH);
		this.holes = new HashMap<>();
		this.cache = new HashMap<>();
	}

	public void forEach(BiConsumer<BlockPos, VoxelShape> consumer) {
		this.cache.forEach(consumer);
	}

	// TODO: I don't like passing the portal in here but it's the easiest way currently
	public VoxelShape get(Level level, BlockPos pos, VoxelShape shape, CollisionContext ctx, PortalInstance.Holder portal) {
		VoxelShape cached = this.cache.get(pos);
		if (cached != null && false) // TODO: need cache invalidation, just don't cache for now
			return cached;

		PatchResult result = this.compute(level, pos, shape, ctx, portal);
		if (result.cacheable) {
			this.cache.put(pos.immutable(), result.shape);
		}

		return result.shape;
	}

	private PatchResult compute(Level level, BlockPos pos, VoxelShape shape, CollisionContext ctx, PortalInstance.Holder portal) {
		// music_disc_otherside reference
		PatchResult otherSide = this.collectOtherSideCollision(level, pos, shape, ctx, portal);
		boolean cacheable = otherSide.cacheable && !level.getBlockState(pos).getBlock().hasDynamicShape();

		// shape - hole + otherSide
		VoxelShape result = Shapes.or(
				Shapes.joinUnoptimized(shape, this.getHole(pos), BooleanOp.ONLY_FIRST),
				otherSide.shape
		);

		return new PatchResult(result, cacheable);
	}

	private PatchResult collectOtherSideCollision(Level level, BlockPos pos, VoxelShape shape, CollisionContext ctx, PortalInstance.Holder portal) {
		// when recursing, don't bother.
		// pretty much impossible to avoid stack overflows.
		if (ctx instanceof PortalCollisionContext) {
			return new PatchResult(Shapes.empty(), true);
		}

		PortalInstance otherPortal = portal.opposite().orElseThrow().portal();
		SinglePortalTransform transform = new SinglePortalTransform(portal.portal(), otherPortal);

		AABB encompassingPos = new AABB(pos);
		AABB searchArea = encompassingPos.inflate(0.5);
		AABB sourceArea = transform.apply(new OBB(searchArea)).encompassingAabb;

		CollisionContext newCtx = PortalCollisionContext.wrap(otherPortal.data.origin(), ctx);

		boolean cacheable = true;
		VoxelShape result = Shapes.empty();

		for (BlockPos sourcePos : BlockPos.betweenClosed(sourceArea)) {
			BlockState state = level.getBlockState(sourcePos);
			cacheable &= !state.getBlock().hasDynamicShape();

			VoxelShape sourceShape = state.getCollisionShape(level, sourcePos, newCtx);
			if (sourceShape.isEmpty())
				continue;

			for (AABB box : sourceShape.toAabbs()) {
				AABB absolute = box.move(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
				AABB transformed = transform.inverse.apply(new OBB(absolute)).encompassingAabb;

				if (transformed.intersects(encompassingPos)) {
					AABB cropped = transformed.intersect(encompassingPos);

					// this isn't perfect but it's good enough
					if (portal.portal().plane.isBehind(cropped.getCenter())) {
						AABB relative = cropped.move(-pos.getX(), -pos.getY(), -pos.getZ());
						result = Shapes.joinUnoptimized(result, Shapes.create(relative), BooleanOp.OR);
					}
				}
			}
		}

		return new PatchResult(result, cacheable);
	}

	private VoxelShape getHole(BlockPos pos) {
		return this.holes.computeIfAbsent(pos.immutable(), pos2 -> VoxelShenanigans.approximateObb(this.hole, pos2));
	}

	private record PatchResult(VoxelShape shape, boolean cacheable) {
	}
}
