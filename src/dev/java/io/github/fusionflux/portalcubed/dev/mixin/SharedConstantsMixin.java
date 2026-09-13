package io.github.fusionflux.portalcubed.dev.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.SharedConstants;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void setInIde(CallbackInfo ci) {
		SharedConstants.IS_RUNNING_IN_IDE = true;
	}
}
