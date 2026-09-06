package io.github.fusionflux.portalcubed.mixin.lemonade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.content.lemon.LemonadeItem;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow
	public abstract ItemStack getUseItem();

	@Shadow
	public abstract void setItemInHand(InteractionHand hand, ItemStack itemStack);

	@Shadow
	public abstract InteractionHand getUsedItemHand();

	@Shadow
	public abstract int getTicksUsingItem();

	@Shadow
	public abstract boolean isUsingItem();

	@Shadow
	public abstract void stopUsingItem();

	@Shadow
	public abstract int getUseItemRemainingTicks();

	@Unique
	private boolean lemonadeArmingFinished;

	@Inject(method = {"releaseUsingItem", "completeUsingItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;stopUsingItem()V"))
	private void dontFinishLemonadeArmingAgain(CallbackInfo ci) {
		this.lemonadeArmingFinished = true;
	}

	@Inject(method = "stopUsingItem", at = @At("HEAD"))
	private void finishLemonadeArmingOnStop(CallbackInfo ci) {
		Level world = ((ItemOwner) this).level();
		if (!world.isClientSide() && !this.lemonadeArmingFinished) {
			ItemStack useItem = this.getUseItem();
			if (useItem.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(useItem)) {
				// setting to true here isn't useless in some rare cases (skeletons for example) setItemInHand might cause another invoke of this method
				this.lemonadeArmingFinished = true;
				ItemStack armed = lemonade.finishArming(useItem, world, (LivingEntity) (Object) this, this.getTicksUsingItem());
				this.setItemInHand(this.getUsedItemHand(), armed);
			}
		}
		this.lemonadeArmingFinished = false;
	}

	@Inject(method = "drop", at = @At("HEAD"), cancellable = true)
	private void drop(ItemStack stack, boolean thrownFromHand, Prediction prediction, CallbackInfoReturnable<ItemEntity> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (stack.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(stack)) {
			Level level = self.level();
			int useDuration = stack.getUseDuration(self);
			if (!this.isUsingItem()) {
				lemonade.finishArming(stack, level, self, useDuration);
			} else {
				lemonade.finishArming(stack, level, self, useDuration - this.getUseItemRemainingTicks());
				this.stopUsingItem();
			}
			cir.setReturnValue(null);
		}
	}
}
