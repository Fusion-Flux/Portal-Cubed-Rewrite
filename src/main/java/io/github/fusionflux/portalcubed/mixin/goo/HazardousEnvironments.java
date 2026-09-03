package io.github.fusionflux.portalcubed.mixin.goo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedFluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

// bulk mixin patching various 'state.is(WATER)' calls to filter out toxic goo
@Mixin({AnimalPanic.class, BottleItem.class, PanicGoal.class, SpongeBlock.class})
public class HazardousEnvironments {
	@Definition(id = "is", method = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z")
	@Definition(id = "WATER", field = "Lnet/minecraft/tags/FluidTags;WATER:Lnet/minecraft/tags/TagKey;")
	@Expression("?.is(WATER)")
	@WrapOperation(
			method = {
					"isWater", // AnimalPanic
					"use", // BottleItem
					"lambda$lookForWater$0", // PanicGoal
					"lambda$removeWaterBreadthFirstSearch$1" // SpongeBlock
			},
			at = @At("MIXINEXTRAS:EXPRESSION"),
			expect = 4, require = 4
	)
	private static boolean gooIsHazardous(FluidState state, TagKey<Fluid> tag, Operation<Boolean> original) {
		return original.call(state, tag) && !state.is(PortalCubedFluidTags.HAZARDOUS_WATER);
	}
}
