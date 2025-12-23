package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@ModifyReturnValue(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN")
	)
	private VoxelShape quantumSpaceHole(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (shape.isEmpty() || !(context instanceof EntityCollisionContext entityCtx))
			return shape;

		Entity entity = entityCtx.getEntity();
		if (entity == null)
			return shape;

		Set<PortalReference> relevantPortals = entity.relevantPortals().get();
		if (relevantPortals.isEmpty())
			return shape;

		List<AABB> boxes = shape.toAabbs();
		if (filter(boxes, relevantPortals, pos)) {
			// at least one box was filtered out. we need to recombine the remaining ones.
			return switch (boxes.size()) {
				case 0 -> Shapes.empty();
				case 1 -> Shapes.create(boxes.getFirst());
				default -> {
					VoxelShape result = Shapes.empty();
					for (AABB box : boxes) {
						result = Shapes.joinUnoptimized(result, Shapes.create(box), BooleanOp.OR);
					}
					yield result.optimize();
				}
			};
		}

		return shape;
	}

	/**
	 * Removes any boxes from {@code boxes} that are behind any of the given portals.
	 * @return true if any boxes were filtered out
	 */
	@Unique
	private static boolean filter(List<AABB> boxes, Set<PortalReference> portals, BlockPos pos) {
		int initialSize = boxes.size();

		for (Iterator<AABB> itr = boxes.iterator(); itr.hasNext();) {
			AABB box = itr.next().move(pos);

			for (PortalReference holder : portals) {
				if (holder.get().plane.isFullyBehindOrOn(box)) {
					itr.remove();
					break;
				}
			}
		}

		return boxes.size() != initialSize;
	}
}
