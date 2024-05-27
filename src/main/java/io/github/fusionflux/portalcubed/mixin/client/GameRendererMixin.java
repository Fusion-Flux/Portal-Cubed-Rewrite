package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = At.Shift.BEFORE))
	private void bigShapePick(float tickDelta, CallbackInfo ci) {
		BigShapeBlock.pick(minecraft.gameMode.getPickRange(), tickDelta);
	}
}
