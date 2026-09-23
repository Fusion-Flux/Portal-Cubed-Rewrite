package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@WrapWithCondition(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;makePoofParticles()V"))
	private boolean noDeathPoofIfDisintegrated(LivingEntity entity) {
		return !Disintegration.isDisintegrating(entity);
	}
}
