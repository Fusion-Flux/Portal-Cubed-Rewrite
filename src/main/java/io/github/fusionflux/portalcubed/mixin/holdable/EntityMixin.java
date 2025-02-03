package io.github.fusionflux.portalcubed.mixin.holdable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.entity.HoldableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract Level level();

	@SuppressWarnings("ConstantValue")
	@ModifyReturnValue(method = "canCollideWith", at = @At("RETURN"))
	private boolean dontCollideWithHeldProp(boolean original, @Local(ordinal = 1, argsOnly = true) Entity other) {
		return original && !((Object) this instanceof Player self && self.getHeldEntity() == other);
	}

	@SuppressWarnings({"ConstantValue", "UnreachableCode"})
	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void dropHeldWhenRemoved(RemovalReason reason, CallbackInfo ci) {
		if (this.level().isClientSide)
			return;

		if ((Object) this instanceof Player player && player.getHeldEntity() != null) {
			player.getHeldEntity().drop();
		} else if ((Object) this instanceof HoldableEntity holdable && holdable.isHeld()) {
			holdable.drop();
		}
	}
}
