package io.github.fusionflux.portalcubed.mixin;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Unique
	private boolean lemonadeArmingFinished;

	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	private void dontDoFallDamageIfBoots(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.getItemBySlot(EquipmentSlot.FEET).is(PortalCubedItemTags.FALL_DAMAGE_RESETTING))
			cir.setReturnValue(false);
	}

	@Inject(method = {"releaseUsingItem", "completeUsingItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;stopUsingItem()V"))
	private void dontFinishLemonadeArmingAgain(CallbackInfo ci) {
		this.lemonadeArmingFinished = true;
	}

	@Inject(method = "stopUsingItem", at = @At("HEAD"))
	private void finishLemonadeArmingOnStop(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		Level level = self.level();
		if (!level.isClientSide && !lemonadeArmingFinished) {
			ItemStack useItem = self.getUseItem();
			if (useItem.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(useItem)) {
				// setting to true here isn't useless in some rare cases (skeletons for example) setItemInHand might cause another invoke of this method
				this.lemonadeArmingFinished = true;
				self.setItemInHand(self.getUsedItemHand(), lemonade.finishArming(useItem, level, self, self.getTicksUsingItem()));
			}
		}
		this.lemonadeArmingFinished = false;
	}
}
