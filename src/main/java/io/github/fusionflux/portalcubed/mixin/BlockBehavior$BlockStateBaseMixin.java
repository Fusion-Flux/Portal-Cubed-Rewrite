package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

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
				return PortalManager.of(level).getCollisionManager().getPortalModifiedShape(shape, pos, entity);
			}
		}
		return shape;
	}
}
