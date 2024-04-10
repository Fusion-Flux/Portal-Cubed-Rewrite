package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.CustomHoldPoseItem;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
	// injecting at return and capturing the local itemstack variable doesn't work because it can't find the locals??
	@Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
	private static void useCustomHoldPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<ArmPose> cir) {
		var stack = player.getItemInHand(hand);
		if (!player.swinging && stack.getItem() instanceof CustomHoldPoseItem customHoldPose)
			cir.setReturnValue(customHoldPose.getHoldPose(stack));
	}
}
