package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "touch", at = @At("HEAD"), cancellable = true)
	private void dontTouchDisintegratingEntities(Entity entity, CallbackInfo ci) {
		if (Disintegration.isDisintegrating(entity)) {
			ci.cancel();
		}
	}
}
