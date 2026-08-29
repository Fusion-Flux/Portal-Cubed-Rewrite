package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.framework.extension.CustomHoldPoseItem;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@WrapMethod(
			method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;"
	)
	private static ArmPose useCustomHoldPose(Avatar avatar, ItemStack itemInHand, InteractionHand hand, Operation<ArmPose> original) {
		if (!avatar.isSwinging() && itemInHand.getItem() instanceof CustomHoldPoseItem customHoldPose) {
			return customHoldPose.getHoldPose(itemInHand);
		}
		return original.call(avatar, itemInHand, hand);
	}
}
