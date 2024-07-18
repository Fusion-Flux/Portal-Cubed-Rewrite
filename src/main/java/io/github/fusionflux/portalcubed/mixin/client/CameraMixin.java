package io.github.fusionflux.portalcubed.mixin.client;

import io.github.fusionflux.portalcubed.content.portal.PortalRenderer;
import net.minecraft.client.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
	@Inject(method = "isDetached", at = @At("RETURN"), cancellable = true)
	private void detachedIfRenderingPortal(CallbackInfoReturnable<Boolean> cir) {
		if (PortalRenderer.isRenderingView())
			cir.setReturnValue(true);
	}
}
