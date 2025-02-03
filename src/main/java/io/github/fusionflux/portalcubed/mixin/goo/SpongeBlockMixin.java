package io.github.fusionflux.portalcubed.mixin.goo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.material.FluidState;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {
	@ModifyExpressionValue(method = "method_49829", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
	private static boolean toxicGooIsNotWater(boolean original, @Local FluidState fluidState) {
		return original && !(fluidState.getType() instanceof GooFluid);
	}
}
