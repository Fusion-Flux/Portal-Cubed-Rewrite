package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

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

	@WrapWithCondition(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;makePoofParticles()V"))
	private boolean noDeathPoofIfDisintegrated(LivingEntity instance) {
		return !instance.pc$disintegrating();
	}
}
