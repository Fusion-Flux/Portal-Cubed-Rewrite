package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.framework.extension.CustomHoldPoseItem;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
	@Inject(
			method = "getArmPose(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void useCustomHoldPose(Player player, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<ArmPose> cir) {
		if (!player.swinging && stack.getItem() instanceof CustomHoldPoseItem customHoldPose)
			cir.setReturnValue(customHoldPose.getHoldPose(stack));
	}
}
