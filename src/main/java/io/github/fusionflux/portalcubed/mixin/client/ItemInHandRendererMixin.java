package io.github.fusionflux.portalcubed.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonAnimator;
import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Inject(
		method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			ordinal = 1
		)
	)
	private void renderArmWithItem(
		AbstractClientPlayer player,
		float tickDelta,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack stack,
		float equipProgress,
		PoseStack matrices,
		MultiBufferSource vertexConsumers,
		int light,
		CallbackInfo ci
	) {
		if (stack.getItem() instanceof ConstructionCannonItem)
			ConstructionCannonAnimator.animate(matrices, tickDelta, hand);
	}
}
