package io.github.fusionflux.portalcubed.mixin.cake;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.block.cake.CustomCandleCakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CandleCakeBlock.class)
public class CandleCakeBlockMixin {
	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
			)
	)
	@SuppressWarnings("ConstantValue")
	private Object dontTouchMap(Map<?, ?> instance, Object k, Object v, Operation<Object> original) {
		if (!((Object) this instanceof CustomCandleCakeBlock)) {
			return original.call(instance, k, v);
		} else {
			return null;
		}
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
			return self.cake.defaultBlockState();
		}
		return state;
	}
}
