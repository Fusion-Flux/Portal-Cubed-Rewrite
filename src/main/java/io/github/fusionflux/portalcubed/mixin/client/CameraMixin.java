package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.fusionflux.portalcubed.content.portal.renderer.PortalRenderer;
import net.minecraft.client.Camera;

@Mixin(Camera.class)
public class CameraMixin {
	@ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
	private boolean detachedIfRenderingPortal(boolean original) {
		return original || PortalRenderer.isRenderingView();
	}
}
