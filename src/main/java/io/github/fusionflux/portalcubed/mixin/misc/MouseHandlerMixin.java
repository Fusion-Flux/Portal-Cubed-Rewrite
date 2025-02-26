package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.render.debug.CameraRotator;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@WrapOperation(
			method = "turnPlayer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
			)
	)
	private void rotat_e(LocalPlayer instance, double yRot, double xRot, Operation<Void> original) {
		if (!CameraRotator.handle(yRot, xRot)) {
			original.call(instance, yRot, xRot);
		}
	}
}
