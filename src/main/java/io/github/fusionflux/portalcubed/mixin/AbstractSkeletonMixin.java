package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.misc.LemonadeAttackGoal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.AbstractSkeleton;

import net.minecraft.world.entity.monster.Monster;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster {
	@Unique
	private final LemonadeAttackGoal<AbstractSkeleton> lemonadeGoal = new LemonadeAttackGoal<>((AbstractSkeleton) (Object) this, 1d, 20, 15f);

	private AbstractSkeletonMixin(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "reassessWeaponGoal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;removeGoal(Lnet/minecraft/world/entity/ai/goal/Goal;)V", ordinal = 0))
	private void removeLemonadeGoal(CallbackInfo ci) {
		this.goalSelector.removeGoal(lemonadeGoal);
	}

	@WrapOperation(method = "reassessWeaponGoal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
	private InteractionHand getLemonadeHoldingHand(LivingEntity entity, Item item, Operation<InteractionHand> original) {
		InteractionHand originalHand = original.call(entity, item);
		if (originalHand == InteractionHand.OFF_HAND)
			return original.call(entity, PortalCubedItems.LEMONADE);
		return originalHand;
	}

	@ModifyExpressionValue(method = "reassessWeaponGoal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private boolean isHoldingLemonade(boolean original, @Local ItemStack heldItem, @Share("lemonade") LocalBooleanRef lemonade) {
		boolean holdingLemonade = heldItem.is(PortalCubedItems.LEMONADE);
		lemonade.set(holdingLemonade);
		return original || holdingLemonade;
	}

	@WrapOperation(method = "reassessWeaponGoal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V", ordinal = 0))
	private void addLemonadeGoal(GoalSelector goalSelector, int priority, Goal goal, Operation<Void> original, @Share("lemonade") LocalBooleanRef lemonade) {
		if (lemonade.get()) {
			original.call(goalSelector, priority, lemonadeGoal);
		} else {
			original.call(goalSelector, priority, goal);
		}
	}
}
