package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.portal.collision.CollisionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehavior$BlockStateBaseMixin {
	@Shadow
	protected abstract BlockState asState();

	@Inject(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN"),
			cancellable = true
	)
	private void staticQuantumSpaceHole(BlockGetter world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		if (world instanceof Level level) {
			CollisionManager manager = CollisionManager.of(level);
			VoxelShape shape = manager.modifyShape(this.asState(), pos, cir.getReturnValue());
			cir.setReturnValue(shape);
		}
	}

	@Inject(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At("RETURN"),
			cancellable = true
	)
	private void dynamicQuantumSpaceHole(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (world instanceof Level level) {
			CollisionManager manager = CollisionManager.of(level);
			VoxelShape shape = manager.modifyShape(this.asState(), pos, cir.getReturnValue());
			cir.setReturnValue(shape);
		}
	}
}
