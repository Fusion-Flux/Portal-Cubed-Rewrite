package io.github.fusionflux.portalcubed.mixin.lemonade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fusionflux.portalcubed.content.lemon.LemonadeItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		if (stack.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(stack)) {
			Level world = this.level();
			int useDuration = stack.getUseDuration(this);
			if (!this.isUsingItem()) {
				lemonade.finishArming(stack, world, this, useDuration);
			} else {
				lemonade.finishArming(stack, world, this, useDuration - this.getUseItemRemainingTicks());
				this.stopUsingItem();
			}
			cir.setReturnValue(null);
		}
	}
}
