package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
	@Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true)
	private static void dontRenderInPortals(Player player, CallbackInfoReturnable<BlockState> cir) {
		if (player.level().portalManager().containsActivePortals(player.getBoundingBox())) {
			cir.setReturnValue(null);
		}
	}
}
