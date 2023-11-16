package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.collision.CollisionManager;
import io.github.fusionflux.portalcubed.content.portal.collision.ShapePatch;
import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.framework.Aaaa;
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
			CollisionManager collisionManager = PortalManager.of(level).getCollisionManager();
			ShapePatch patch = collisionManager.getPatchAt(pos);
			if (patch != null) {
				patch.apply(shape, entityCtx);
			}
		}
	}
}
