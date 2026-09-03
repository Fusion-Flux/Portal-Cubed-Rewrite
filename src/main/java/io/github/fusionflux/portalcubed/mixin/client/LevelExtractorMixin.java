package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
	@Shadow
	private ClientLevel level;

	@WrapOperation(method = "extractBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private boolean extractMultiBlockDestroyAnimation(List<BlockBreakingRenderState> instance, Object object, Operation<Boolean> original) {
		BlockBreakingRenderState renderState = (BlockBreakingRenderState) object;
		BlockState state = renderState.blockState();
		if (state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			BlockPos pos = renderState.blockPos();
			int progress = renderState.progress();
			for (BlockPos quadrantPos : multiBlock.quadrants(multiBlock.getOriginPos(pos, state), state)) {
				original.call(instance, new BlockBreakingRenderState(quadrantPos, this.level.getBlockState(quadrantPos), progress));
			}
			return true;
		}

		return original.call(instance, object);
	}

	@WrapOperation(
			method = "lambda$getViewBlockingState$1",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;isViewBlocking(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/AABB;)Z"
			)
	)
	private static boolean dontBlockViewIfPortalPresent(BlockState instance, BlockGetter blockGetter, BlockPos pos, AABB area, Operation<Boolean> original) {
		boolean isViewBlocking = original.call(instance, blockGetter, pos, area);
		if (!isViewBlocking)
			return false;

		if (!(blockGetter instanceof Level level)) {
			// this comes from player.level(), but the lambda only captures it as a BlockGetter
			return true;
		}

		return !level.portalManager().containsActivePortals(new AABB(pos));
	}
}
