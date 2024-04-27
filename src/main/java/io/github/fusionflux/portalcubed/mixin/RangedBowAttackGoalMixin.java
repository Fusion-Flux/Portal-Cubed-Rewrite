package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.misc.Lemonade;
import io.github.fusionflux.portalcubed.content.misc.LemonadeAttackGoal;
import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;

import net.minecraft.world.entity.monster.Monster;

import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedBowAttackGoal.class)
public class RangedBowAttackGoalMixin {
	@Unique
	private final boolean isLemonadeAttackGoal = (Object) this instanceof LemonadeAttackGoal<?>;

	@Shadow
	@Final
	private Monster mob;

	@Inject(method = "isHoldingBow", at = @At("RETURN"), cancellable = true)
	private void isHoldingLemonade(CallbackInfoReturnable<Boolean> cir) {
		if (isLemonadeAttackGoal && this.mob.isHolding(PortalCubedItems.LEMONADE))
			cir.setReturnValue(true);
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
	private int useVariableThrowTime(int original) {
		if (isLemonadeAttackGoal)
			return ((LemonadeAttackGoal<?>) (Object) this).timeUntilThrow;
		return original;
	}

	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"))
	private boolean finishArmingInsteadOfArrow(RangedAttackMob instance, LivingEntity target, float power) {
		return !isLemonadeAttackGoal;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
	private InteractionHand getLemonadeHoldingHand(LivingEntity entity, Item item, Operation<InteractionHand> original) {
		if (isLemonadeAttackGoal)
			return original.call(entity, PortalCubedItems.LEMONADE);
		return original.call(entity, item);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;startUsingItem(Lnet/minecraft/world/InteractionHand;)V"))
	private void armLemonade(Monster actor, InteractionHand hand, Operation<Void> original) {
		if (isLemonadeAttackGoal) {
			// 1 second offset to make self explosion slightly less rare
			((LemonadeAttackGoal<?>) (Object) this).timeUntilThrow = actor.getRandom().nextInt(Lemonade.TICKS_PER_TICK, Lemonade.MAX_ARM_TIME + Lemonade.TICKS_PER_TICK);
			actor.setItemInHand(hand, LemonadeItem.setArmed(true, actor.getItemInHand(hand)));
		}
		original.call(actor, hand);
	}
}
