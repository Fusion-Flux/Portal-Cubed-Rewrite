package io.github.fusionflux.portalcubed.mixin.goo;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedFluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;

import net.minecraft.world.level.BlockGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(AnimalPanic.class)
public class AnimalPanicMixin {
	@ModifyArg(
		method = "lookForWater",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/BlockPos;findClosestMatch(Lnet/minecraft/core/BlockPos;IILjava/util/function/Predicate;)Ljava/util/Optional;"
		)
	)
	private Predicate<BlockPos> toxicGooIsNotFineToPathfindTo(Predicate<BlockPos> posFilter, @Local(argsOnly = true) BlockGetter level) {
		return posFilter.and(p -> !level.getFluidState(p).is(PortalCubedFluidTags.HAZARDOUS_WATER));
	}
}
