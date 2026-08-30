package io.github.fusionflux.portalcubed.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
	@Shadow
	private ClientLevel level;

	@Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
	@Definition(id = "levelRenderState", local = @Local(type = LevelRenderState.class, name = "levelRenderState", argsOnly = true))
	@Definition(id = "blockBreakingRenderStates", field = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;blockBreakingRenderStates:Ljava/util/List;")
	@Expression("levelRenderState.blockBreakingRenderStates.add(?)")
	@WrapOperation(method = "extractBlockDestroyAnimation", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean renderMultiBlockBreakingTexture(@SuppressWarnings("rawtypes") List instance, @Coerce Object e, Operation<Boolean> original) {
		BlockBreakingRenderState renderState = (BlockBreakingRenderState) e;
		BlockState state = renderState.blockState();
		if (state.getBlock() instanceof AbstractMultiBlock multiBlock) {
			BlockPos pos = renderState.blockPos();
			int progress = renderState.progress();
			for (BlockPos quadrantPos : multiBlock.quadrants(multiBlock.getOriginPos(pos, state), state)) {
				original.call(instance, new BlockBreakingRenderState(quadrantPos, this.level.getBlockState(quadrantPos), progress));
			}
			return true;
		} else {
			return original.call(instance, e);
		}
	}
}
