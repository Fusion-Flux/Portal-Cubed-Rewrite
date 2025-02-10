package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunItem;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Shadow
	private ItemStack mainHandItem;

	@Shadow
	private ItemStack offHandItem;

	@WrapMethod(method = "itemUsed")
	private void noUseAnimationForPortalGun(InteractionHand hand, Operation<Void> original) {
		ItemStack handItem = hand == InteractionHand.MAIN_HAND ? this.mainHandItem : this.offHandItem;
		if (!(handItem.getItem() instanceof PortalGunItem))
			original.call(hand);
	}
}
