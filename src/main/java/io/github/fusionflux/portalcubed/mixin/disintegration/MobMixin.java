package io.github.fusionflux.portalcubed.mixin.disintegration;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionParams.AfterConversion;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public class MobMixin {
	@WrapMethod(method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;")
	private <T extends Mob> @Nullable T wrapConvert(EntityType<T> type, ConversionParams params, EntitySpawnReason reason, AfterConversion<T> callback, Operation<T> original) {
		return Disintegration.handleConversion((Mob) (Object) this, params, () -> original.call(type, params, reason, callback));
	}
}
