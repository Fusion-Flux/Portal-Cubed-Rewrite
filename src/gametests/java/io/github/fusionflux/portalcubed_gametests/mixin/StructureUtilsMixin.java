package io.github.fusionflux.portalcubed_gametests.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.StructureUtils;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructureUtils.class)
public class StructureUtilsMixin {
	@ModifyExpressionValue(
			method = "getBoundingBoxAtGround",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
			)
	)
	private static BlockPos justUseTheGivenPos(BlockPos garbage, BlockPos pos, int radius, ServerLevel level) {
		return pos;
	}
}
