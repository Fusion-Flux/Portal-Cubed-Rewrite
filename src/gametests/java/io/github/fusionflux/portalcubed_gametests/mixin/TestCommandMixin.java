package io.github.fusionflux.portalcubed_gametests.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.TestCommand;

import net.minecraft.world.level.block.entity.StructureBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TestCommand.class)
public class TestCommandMixin {
	@ModifyExpressionValue(
			method = "showPos",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/BlockPos;subtract(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;"
			)
	)
	private static BlockPos accountForStructureOffset(BlockPos original, @Local StructureBlockEntity be) {
		return original.subtract(be.getStructurePos());
	}
}
