package io.github.fusionflux.portalcubed.mixin.utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {
	@WrapOperation(
			method = "method_17743", // first lambda in clip
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/BlockGetter;clipWithInteractionOverride(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/phys/BlockHitResult;"
			)
	)
	private BlockHitResult maybeSkipInteractionOverride(BlockGetter getter, Vec3 from, Vec3 to, BlockPos pos,
														VoxelShape shape, BlockState state, Operation<BlockHitResult> original,
														ClipContext ctx, BlockPos posAgain) {
		BlockState actualState = state;

		if (ctx.pc$ignoreInteractionOverride()) {
			// this will cause the interaction shape to be empty, ignoring it
			actualState = Blocks.AIR.defaultBlockState();
		}

		return original.call(getter, from, to, pos, shape, actualState);
	}
}
