package io.github.fusionflux.portalcubed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.class)
public class EntityTypeMixin {
	@ModifyExpressionValue(
			method = "create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;wrapDegrees(F)F")
	)
	private float dontRandomlyRotateProps(float original, @Local Entity entity) {
		return entity instanceof Prop ? 0 : original;
	}
}
