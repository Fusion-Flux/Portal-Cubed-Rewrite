package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.content.misc.LemonadeItem;
import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fusionflux.portalcubed.framework.entity.FollowingSoundInstance;
import io.github.fusionflux.portalcubed.framework.extension.PlayerExt;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin implements PlayerExt {
	@Unique
	@Nullable
	private HoldableEntity heldEntity;

	@ClientOnly
	@Unique
	private int grabSoundTimer = 0;
	@ClientOnly
	@Unique
	@Nullable
	private FollowingSoundInstance grabSound = null;
	@ClientOnly
	@Unique
	@Nullable
	private FollowingSoundInstance holdLoopSound = null;

	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		if (stack.getItem() instanceof LemonadeItem lemonade && LemonadeItem.isArmed(stack)) {
			Player self = (Player) (Object) this;
			Level level = self.level();
			if (!self.isUsingItem()) {
				lemonade.finishArming(stack, level, self, stack.getUseDuration());
			} else {
				lemonade.finishArming(stack, level, self, stack.getUseDuration() - self.getUseItemRemainingTicks());
				self.stopUsingItem();
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
	public void pc$grabSoundTimer(int timer) {
		grabSoundTimer = timer;
	}

	@Override
	public int pc$grabSoundTimer() {
		return grabSoundTimer;
	}

	@Override
	public void pc$grabSound(Object grabSound) {
		this.grabSound = (FollowingSoundInstance) grabSound;
	}

	@Override
	public Object pc$grabSound() {
		return grabSound;
	}

	@Override
	public void pc$holdLoopSound(Object holdLoopSound) {
		this.holdLoopSound = (FollowingSoundInstance) holdLoopSound;
	}

	@Override
	public Object pc$holdLoopSound() {
		return holdLoopSound;
	}
}
