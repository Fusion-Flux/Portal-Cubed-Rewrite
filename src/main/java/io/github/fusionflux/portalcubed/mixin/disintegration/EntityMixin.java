package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import io.github.fusionflux.portalcubed.framework.entity.EntityTickWrapper;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void wrapEntityTick(Entity instance, Operation<Void> original) {
		EntityTickWrapper.handle(instance, original);
	}

	@ModifyReturnValue(method = "isIgnoringBlockTriggers", at = @At("RETURN"))
	private boolean ignoreBlockTriggersIfDisintegrating(boolean original) {
		return original || this.isDisintegrating();
	}

	@ModifyReturnValue(method = "isNoGravity", at = @At("RETURN"))
	private boolean noGravityIfDisintegrating(boolean original) {
		return original || this.isDisintegrating();
	}

	@ModifyReturnValue(method = "isSilent", at = @At("RETURN"))
	private boolean silentIfDisintegrating(boolean original) {
		return original || this.isDisintegrating();
	}

	@ModifyReturnValue(method = "isInvulnerable", at = @At("RETURN"))
	private boolean invulnerableIfDisintegrating(boolean original) {
		return original || this.isDisintegrating();
	}

	@ModifyReturnValue(method = "isAlive", at = @At("RETURN"))
	private boolean notAliveIfDisintegrating(boolean original) {
		return original && !this.isDisintegrating();
	}

	@Unique
	private boolean isDisintegrating() {
		return Disintegration.isDisintegrating((Entity) (Object) this);
	}
}
