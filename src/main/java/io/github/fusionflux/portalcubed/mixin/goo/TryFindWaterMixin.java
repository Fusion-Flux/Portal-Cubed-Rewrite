package io.github.fusionflux.portalcubed.mixin.goo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedFluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.ai.behavior.TryFindWater;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(TryFindWater.class)
public class TryFindWaterMixin {
	@WrapOperation(
			method = "method_47179",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
			)
	)
	private static boolean gooIsNotWater(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
		return original.call(instance, tag) && !original.call(instance, PortalCubedFluidTags.HAZARDOUS_WATER);
	}
}
