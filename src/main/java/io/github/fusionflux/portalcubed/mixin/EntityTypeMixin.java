package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {
	@Shadow
	public abstract boolean is(TagKey<EntityType<?>> tag);

	@ModifyExpressionValue(
			method = "create(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/RandomSource;nextFloat()F"
			)
	)
	private float maybeDontRotate(float original) {
		return this.is(PortalCubedEntityTags.DONT_ROTATE_RANDOMLY) ? 0 : original;
	}
}
