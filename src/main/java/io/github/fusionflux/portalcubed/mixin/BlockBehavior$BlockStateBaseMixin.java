package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import io.github.fusionflux.portalcubed.framework.shape.VoxelShenanigans;

import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@Shadow
	public abstract Block getBlock();

	@ModifyReturnValue(
			method = {
					"getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
					"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
					"getVisualShape"
			},
			at = @At("RETURN")
	)
	private VoxelShape quantumSpaceHole(VoxelShape shape, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (world instanceof Level level && context instanceof EntityCollisionContext entityCtx) {
			Entity entity = entityCtx.getEntity();
			if (entity != null) {
				PortalManager manager = PortalManager.of(level);
				Set<Portal> portals = manager.getPortalsAt(pos);
				if (!portals.isEmpty()) {
					MutableObject<VoxelShape> shapeHolder = new MutableObject<>(shape);
					for (Portal portal : portals) {
						Portal linked = portal.getLinked();
						if (linked == null)
							continue;
						if (!portal.collisionArea.contains(entity.position()))
							continue;
						BlockPos.betweenClosedStream(linked.collisionArea).forEach(blockPos -> {
							BlockState state = level.getBlockState(blockPos);
							VoxelShape otherShape = state.getCollisionShape(level, blockPos, context);
							VoxelShape transformed = VoxelShenanigans.transformShapeAcross(otherShape, linked, portal);
							// limit to 1x1
							VoxelShape limited = Shapes.join(Shapes.block(), transformed, BooleanOp.AND);
							// merge
							VoxelShape merged = Shapes.or(shapeHolder.getValue(), limited);
							shapeHolder.setValue(merged);
						});
					}
					return shapeHolder.getValue();
				}
			}
		}
		return shape;
	}
}
