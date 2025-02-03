package io.github.fusionflux.portalcubed.mixin.lemonade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.lemon.LemonadeAttackGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster {
	protected AbstractSkeletonMixin(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	private LemonadeAttackGoal<AbstractSkeleton> lemonadeGoal;

	@Inject(method = "<init>", at = @At("CTOR_HEAD"))
	protected void init(CallbackInfo info) {
		this.lemonadeGoal = new LemonadeAttackGoal<>((AbstractSkeleton) (Object) this, 1, 20, 15);
	}

	@Inject(
			method = "reassessWeaponGoal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;removeGoal(Lnet/minecraft/world/entity/ai/goal/Goal;)V",
					ordinal = 0
			)
	)
	private void removeLemonadeGoal(CallbackInfo ci) {
		this.goalSelector.removeGoal(this.lemonadeGoal);
	}

	@ModifyArg(
			method = "reassessWeaponGoal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V",
					ordinal = 1
			),
			index = 1
	)
	private Goal addLemonadeGoal(Goal goal) {
		ItemStack heldItem = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, PortalCubedItems.LEMONADE));
		return heldItem.is(PortalCubedItems.LEMONADE) ? this.lemonadeGoal : goal;
	}
}
