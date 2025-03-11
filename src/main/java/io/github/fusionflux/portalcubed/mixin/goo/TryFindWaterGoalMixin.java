package io.github.fusionflux.portalcubed.mixin.goo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedFluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TryFindWaterGoal.class)
public class TryFindWaterGoalMixin {
	@WrapOperation(
		method = "canUse",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
		)
	)
	private boolean gooIsNotWater(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
		return original.call(instance, tag) && !original.call(instance, PortalCubedFluidTags.HAZARDOUS_WATER);
	}
}
