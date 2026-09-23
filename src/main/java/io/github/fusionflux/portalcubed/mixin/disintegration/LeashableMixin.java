package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;

@Mixin(Leashable.class)
public interface LeashableMixin {
	@WrapOperation(
			// we can't just inject into canBeLeashed unfortunately since it's overridden without calling super
			method = "canHaveALeashAttachedTo",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;canBeLeashed()Z")
	)
	private boolean cannotLeashDisintegratingEntities(Leashable entity, Operation<Boolean> original) {
		return original.call(entity) && !Disintegration.isDisintegrating((Entity) entity);
	}
}
