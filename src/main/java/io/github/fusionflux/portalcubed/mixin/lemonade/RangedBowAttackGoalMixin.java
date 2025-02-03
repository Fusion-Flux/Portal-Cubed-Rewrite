package io.github.fusionflux.portalcubed.mixin.lemonade;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.lemon.Lemonade;
import io.github.fusionflux.portalcubed.content.lemon.LemonadeAttackGoal;
import io.github.fusionflux.portalcubed.content.lemon.LemonadeItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;

@Mixin(RangedBowAttackGoal.class)
public class RangedBowAttackGoalMixin {
	@Shadow
	@Final
	private Monster mob;

	@Unique
	private final boolean lemonade = (Object) this instanceof LemonadeAttackGoal<?>;

	@ModifyArg(
			method = "isHoldingBow",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/monster/Monster;isHolding(Lnet/minecraft/world/item/Item;)Z"
			)
	)
	private Item holdingLemonade(Item item) {
		return this.lemonade ? PortalCubedItems.LEMONADE : item;
	}

	@SuppressWarnings("DataFlowIssue")
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 20, ordinal = 2))
	private int useVariableThrowTime(int original) {
		return this.lemonade ? ((LemonadeAttackGoal<?>) (Object) this).timeUntilThrow : original;
	}

	@WrapWithCondition(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
			)
	)
	private boolean finishArmingInsteadOfShooting(RangedAttackMob instance, LivingEntity target, float power) {
		return !this.lemonade;
	}

	@ModifyArg(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"
			),
			index = 1
	)
	private Item lemonadeIsYourWeapon(Item weapon) {
		return this.lemonade ? PortalCubedItems.LEMONADE : weapon;
	}

	@SuppressWarnings("DataFlowIssue")
	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/monster/Monster;startUsingItem(Lnet/minecraft/world/InteractionHand;)V"
			),
			cancellable = true
	)
	private void armLemonade(CallbackInfo ci) {
		if (this.lemonade) {
			// 1 second offset to make self explosion slightly less rare
			((LemonadeAttackGoal<?>) (Object) this).timeUntilThrow = this.mob
					.getRandom()
					.nextInt(Lemonade.TICKS_PER_TIMER_TICK, Lemonade.MAX_ARM_TIME + Lemonade.TICKS_PER_TIMER_TICK);

			InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this.mob, PortalCubedItems.LEMONADE);
			this.mob.setItemInHand(hand, LemonadeItem.setArmed(true, this.mob.getItemInHand(hand)));
			this.mob.startUsingItem(hand);

			ci.cancel();
		}
	}
}
