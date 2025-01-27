package io.github.fusionflux.portalcubed_gametests.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.StructureUtils;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StructureUtils.class)
public class StructureUtilsMixin {
	@WrapOperation(
			method = "getBoundingBoxAtGround",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
			)
	)
	private static BlockPos justUseTheGivenPos(double x, double y, double z, Operation<BlockPos> original,
											   BlockPos pos, int radius, ServerLevel level) {
		return pos;
	}

	@ModifyConstant(method = "getBoundingBoxAtGround", constant = @Constant(intValue = 10))
	private static int justUseTheGivenRadius(int constant, BlockPos pos, int radius, ServerLevel level) {
		return radius;
	}
}
