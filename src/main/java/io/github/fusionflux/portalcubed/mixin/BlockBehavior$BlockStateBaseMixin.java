package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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

	@Inject(
			method = {
					"getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
					"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
					"getVisualShape"
			},
			at = @At("RETURN"),
			cancellable = true
	)
	private void quantumSpaceHole(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (world instanceof Level level && context instanceof EntityCollisionContext entityCtx) {
			Entity entity = entityCtx.getEntity();
			if (entity != null) {
				PortalManager manager = PortalManager.of(level);
				Set<Portal> portals = manager.getPortalsAt(pos);
				if (!portals.isEmpty()) {
					VoxelShape shape = cir.getReturnValue();
					// move shape to pos
					shape = shape.move(pos.getX(), pos.getY(), pos.getZ());
					for (Portal portal : portals) {
						shape = Shapes.join(shape, portal.hole, BooleanOp.ONLY_FIRST);
					}
					// move shape back to relative coords
					shape = shape.move(-pos.getX(), -pos.getY(), -pos.getZ());
					cir.setReturnValue(shape);
				}
			}
		}
	}
}
