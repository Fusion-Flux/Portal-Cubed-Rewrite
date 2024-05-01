package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import net.minecraft.util.Mth;
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

	@Inject(
			method = "causeFallDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V",
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	private void dontDoFallDamageIfBoots(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir, @Local int fallDamage) {
		LivingEntity self = (LivingEntity) (Object) this;
		ItemStack boots = self.getItemBySlot(EquipmentSlot.FEET);
		if (boots.is(PortalCubedItemTags.ABSORB_FALL_DAMAGE)) {
			// use fall damage here to include jump boost, safe fall distance, and the damage multiplier.
			int wantedBootDamage = Mth.ceil(fallDamage / 2f);

			// force break when over durability to prevent randomly surviving falls due to unbreaking.
			if (boots.isDamageableItem() && (wantedBootDamage >= (boots.getMaxDamage() - boots.getDamageValue()))) {
				boots.shrink(1);
				boots.setDamageValue(0);
				self.broadcastBreakEvent(EquipmentSlot.FEET);
				return;
			}

			boots.hurtAndBreak(wantedBootDamage, self, e -> e.broadcastBreakEvent(EquipmentSlot.FEET));

			if (!boots.isEmpty())
				cir.setReturnValue(false);
		}
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
