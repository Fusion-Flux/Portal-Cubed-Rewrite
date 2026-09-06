package io.github.fusionflux.portalcubed.mixin.utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {
	@ModifyExpressionValue(
			method = "create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PostSpawnProcessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/RandomSource;nextFloat()F"
			)
	)
	private float maybeDontRotate(float original, @Local(name = "entity") Entity entity) {
		return entity.is(PortalCubedEntityTags.DONT_ROTATE_RANDOMLY) ? 0 : original;
	}
}
