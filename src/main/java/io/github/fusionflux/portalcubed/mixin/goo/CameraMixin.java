package io.github.fusionflux.portalcubed.mixin.goo;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;

@Mixin(Camera.class)
public class CameraMixin {
	@ModifyExpressionValue(
			method = "getFluidInCamera",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/world/level/material/FogType;WATER:Lnet/minecraft/world/level/material/FogType;",
					opcode = Opcodes.GETSTATIC
			)
	)
	private FogType gooFogType(FogType original, @Local(ordinal = 0) FluidState fluid) {
		return fluid.getType() instanceof GooFluid ? FogType.PORTALCUBED_GOO : original;
	}
}
