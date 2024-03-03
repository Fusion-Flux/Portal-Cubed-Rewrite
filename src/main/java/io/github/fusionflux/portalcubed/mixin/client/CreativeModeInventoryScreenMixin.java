package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void floorButtonEasterEgg(CallbackInfo ci) {
		if (FloorButtonBlock.easterEggTrigger < 2)
			++FloorButtonBlock.easterEggTrigger;
	}
}
