package io.github.fusionflux.portalcubed.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.content.lemon.LemonadeItem;
import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerExt {
	@Unique
	@Nullable
	private HoldableEntity heldEntity;

	@Unique
	private boolean hasSubmergedTheOperationalEndOfTheDevice;

	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		if (stack.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(stack)) {
			Level level = this.level();
			int useDuration = stack.getUseDuration(this);
			if (!this.isUsingItem()) {
				lemonade.finishArming(stack, level, this, useDuration);
			} else {
				lemonade.finishArming(stack, level, this, useDuration - this.getUseItemRemainingTicks());
				this.stopUsingItem();
			}
			cir.setReturnValue(null);
		}
	}

	@WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;touch(Lnet/minecraft/world/entity/Entity;)V"))
	private boolean cantTouchDisintegratingEntities(Player player, Entity entity) {
		return !entity.pc$disintegrating();
	}

	@Override
	public void setHeldEntity(@Nullable HoldableEntity heldEntity) {
		this.heldEntity = heldEntity;
	}

	@Override
	@Nullable
	public HoldableEntity getHeldEntity() {
		return this.heldEntity;
	}

	@Override
	public void pc$setHasSubmergedTheOperationalEndOfTheDevice(boolean hasSubmergedTheOperationalEndOfTheDevice) {
		this.hasSubmergedTheOperationalEndOfTheDevice = hasSubmergedTheOperationalEndOfTheDevice;
	}

	@Override
	public boolean pc$hasSubmergedTheOperationalEndOfTheDevice() {
		return this.hasSubmergedTheOperationalEndOfTheDevice;
	}
}
