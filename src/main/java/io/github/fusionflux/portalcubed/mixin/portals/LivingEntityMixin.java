package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.framework.extension.LivingEntityExt;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityExt {
	@Unique
	private boolean skipWakeUpMovement;

	@WrapWithCondition(
			method = { "stopSleeping", "method_18404" },
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;setPos(DDD)V"
			)
	)
	private boolean shouldSetPos(LivingEntity instance, double x, double y, double z) {
		return !this.skipWakeUpMovement;
	}

	@WrapWithCondition(
			method = "method_18404",
			at = {
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/entity/LivingEntity;setXRot(F)V"
					),
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/entity/LivingEntity;setYRot(F)V"
					)
			}
	)
	private boolean shouldSetRot(LivingEntity instance, float rot) {
		return !this.skipWakeUpMovement;
	}

	@Override
	public void pc$skipWakeUpMovement(boolean value) {
		this.skipWakeUpMovement = value;
	}
}
