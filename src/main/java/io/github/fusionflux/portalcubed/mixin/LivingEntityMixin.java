package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Unique
	private boolean lemonadeArmingFinished;

	@Inject(method = {"releaseUsingItem", "completeUsingItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;stopUsingItem()V"))
	private void dontFinishLemonadeArmingAgain(CallbackInfo ci) {
		lemonadeArmingFinished = true;
	}

	@Inject(method = "stopUsingItem", at = @At("HEAD"))
	private void finishLemonadeArmingOnStop(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		Level level = self.level();
		if (!level.isClientSide && !lemonadeArmingFinished) {
			ItemStack useItem = self.getUseItem();
			if (useItem.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(useItem))
				self.setItemInHand(self.getUsedItemHand(), lemonade.finishArming(useItem, self.level(), self, self.getTicksUsingItem()));
		}
		lemonadeArmingFinished = false;
	}
}
