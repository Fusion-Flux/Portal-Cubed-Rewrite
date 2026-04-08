package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 16), allow = 1)
	private static int increaseMatrixStackSize(int constant) {
		return 32;
	}
}
