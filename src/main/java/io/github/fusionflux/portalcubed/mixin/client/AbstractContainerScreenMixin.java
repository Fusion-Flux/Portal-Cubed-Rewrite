package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.button.P1FloorButtonBlockItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(method = "onClose", at = @At("RETURN"))
	private void floorButtonEasterEgg(CallbackInfo ci) {
		P1FloorButtonBlockItem.easterEgg = false;
	}
}
