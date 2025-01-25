package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.extension.DisintegrationExt;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public class MobMixin implements DisintegrationExt {
	@WrapOperation(
			method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/ConversionType;convert(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/ConversionParams;)V"
			)
	)
	private void dontCopyDisintegrationState(ConversionType instance, Mob oldMob, Mob newMob, ConversionParams conversionParams, Operation<Void> original) {
		boolean wasDisintegrating = this.pc$disintegrating();
		this.pc$disintegrating(false);
		original.call(instance, oldMob, newMob, conversionParams);
		this.pc$disintegrating(wasDisintegrating);
	}
}
