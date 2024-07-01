package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.content.misc.LongFallBoots;
import io.github.fusionflux.portalcubed.content.misc.SourcePhysics;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedDamageTypeTags;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedItemTags;
import io.github.fusionflux.portalcubed.framework.extension.ItemStackExt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@Shadow
	public abstract ItemStack getItemBySlot(EquipmentSlot slot);

	@Shadow
	public abstract ItemStack getUseItem();

	@Shadow
	public abstract void setItemInHand(InteractionHand hand, ItemStack stack);

	@Shadow
	public abstract InteractionHand getUsedItemHand();

	@Shadow
	public abstract int getTicksUsingItem();

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
	private void absorbFallDamageIntoBoots(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir, @Local int fallDamage) {
		ItemStack boots = this.getItemBySlot(EquipmentSlot.FEET);
		if (boots.is(PortalCubedItemTags.ABSORB_FALL_DAMAGE) && !damageSource.is(PortalCubedDamageTypeTags.BYPASSES_FALL_DAMAGE_ABSORPTION)) {
			// use fall damage here to include jump boost, safe fall distance, and the damage multiplier.
			int bootDamage = LongFallBoots.calculateFallDamage(boots, fallDamage);
			((ItemStackExt) (Object) boots).pc$hurtAndBreakNoUnbreaking(
					bootDamage, (LivingEntity) (Object) this, e -> e.broadcastBreakEvent(EquipmentSlot.FEET)
			);

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
		Level level = this.level();
		if (!level.isClientSide && !lemonadeArmingFinished) {
			ItemStack useItem = this.getUseItem();
			if (useItem.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(useItem)) {
				// setting to true here isn't useless in some rare cases (skeletons for example) setItemInHand might cause another invoke of this method
				this.lemonadeArmingFinished = true;
				ItemStack armed = lemonade.finishArming(useItem, level, (LivingEntity) (Object) this, this.getTicksUsingItem());
				this.setItemInHand(this.getUsedItemHand(), armed);
			}
		}
		this.lemonadeArmingFinished = false;
	}

	@WrapWithCondition(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;makePoofParticles()V"))
	private boolean noDeathPoofIfDisintegrated(LivingEntity instance) {
		return !instance.pc$disintegrating();
	}

	@ModifyExpressionValue(
			method = "jumpFromGround",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"
			)
	)
	private boolean sourcePhysicsNoSprintBoost(boolean original) {
		return !((Object) this instanceof Player player) || !SourcePhysics.appliesTo(player);
	}

	@WrapOperation(
			method = "travel",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private Vec3 sourcePhysicsFriction(LivingEntity self, Vec3 movementInput, float slipperiness, Operation<Vec3> original,
									   @Local(ordinal = 0) float blockFriction,
									   @Local(ordinal = 1) LocalFloatRef friction) {
		if ((Object) this instanceof Player player && SourcePhysics.appliesTo(player)) {
			boolean wasGrounded = self.onGround();
			Vec3 newVel = original.call(self, movementInput, slipperiness);
			boolean isGrounded = self.onGround();
			if (!isGrounded) {
				// when airborne, discard all friction to maintain speed.
				friction.set(1);
			}
			if (!wasGrounded && isGrounded) {
				// when landing, re-calculate friction.
				// Otherwise, air friction is used for an extra tick, building infinite speed.
				friction.set(blockFriction * 0.91f);
			}
			return newVel;
		}

		// no source physics, change nothing
		return original.call(self, movementInput, slipperiness);
	}
}
