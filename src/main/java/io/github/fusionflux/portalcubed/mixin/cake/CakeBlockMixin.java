package io.github.fusionflux.portalcubed.mixin.cake;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.block.cake.CustomCakeBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
	@WrapOperation(
			method = "useItemOn",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/CandleCakeBlock;byCandle(Lnet/minecraft/world/level/block/CandleBlock;)Lnet/minecraft/world/level/block/state/BlockState;"
			)
	)
	private BlockState modifyCandled(CandleBlock candle, Operation<BlockState> original) {
		if ((Object) this instanceof CustomCakeBlock custom) {
			return custom.getWithCandle(candle).defaultBlockState();
		}
		return original.call(candle);
	}
}
