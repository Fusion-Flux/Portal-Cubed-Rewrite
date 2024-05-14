package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.content.misc.LongFallBoots;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Unique
	private static final double ANTI_FRICTION_OFFSET = 2;
	@Unique
	private static final double FRICTION_SCALING = 1;
	@Unique
	private static final double VANILLA_AIR_FRICTION = 0.91;

	@Unique
	private boolean lemonadeArmingFinished;

	@WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 dragTest(LivingEntity instance, Vec3 movementInput, float slipperiness, Operation<Vec3> original) {
		ItemStack boots = instance.getItemBySlot(EquipmentSlot.FEET);
		if (!instance.onGround() && boots.is(PortalCubedItemTags.ABSORB_FALL_DAMAGE)) {
			double speed = instance.getDeltaMovement().length();
			double antiFriction = ((1 + VANILLA_AIR_FRICTION) + ANTI_FRICTION_OFFSET) / (speed * FRICTION_SCALING);
			return original.call(instance, movementInput.multiply(antiFriction, 1, antiFriction), slipperiness);
		} else {
			return original.call(instance, movementInput, slipperiness);
		}
	}

	@Inject(
			method = "causeFallDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V",
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	private void absorbFallDamageIntoBoots(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir, @Local int fallDamage) {
		LivingEntity self = (LivingEntity) (Object) this;
		ItemStack boots = self.getItemBySlot(EquipmentSlot.FEET);
		if (boots.is(PortalCubedItemTags.ABSORB_FALL_DAMAGE)) {
			// use fall damage here to include jump boost, safe fall distance, and the damage multiplier.
			int bootDamage = LongFallBoots.calculateFallDamage(boots, fallDamage);
			((ItemStackExt) (Object) boots).pc$hurtAndBreakNoUnbreaking(bootDamage, self, e -> e.broadcastBreakEvent(EquipmentSlot.FEET));

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
