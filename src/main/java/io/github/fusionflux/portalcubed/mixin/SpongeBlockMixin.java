package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.SpongeBlock;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {
	@WrapOperation(method = "method_49829", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
	private static boolean toxicGooIsNotWater(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
		return original.call(instance, tag) && !(instance.getType() instanceof GooFluid);
	}
}
