package io.github.fusionflux.portalcubed.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.framework.block.cake.CustomCandleCakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CandleCakeBlock.class)
public class CandleCakeBlockMixin {
	@WrapWithCondition(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
			)
	)
	@SuppressWarnings("ConstantValue")
	private boolean dontTouchMap(Map<?, ?> instance, Object k, Object v) {
		return !((Object) this instanceof CustomCandleCakeBlock);
	}

	@ModifyArg(
			method = "useWithoutItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/CakeBlock;eat(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;"
			)
	)
	private BlockState changeCake(BlockState state) {
		if ((Object) this instanceof CustomCandleCakeBlock self) {
			return self.getCake().defaultBlockState();
		}
		return state;
	}
}
