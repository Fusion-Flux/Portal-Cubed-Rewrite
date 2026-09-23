package io.github.fusionflux.portalcubed.mixin.disintegration;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.fusionflux.portalcubed.content.fizzler.Disintegration;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;

/// We do this instead of injecting into [Entity#canCollideWith(Entity)] and [Entity#canBeCollidedWith(Entity)]
/// since both of those are overriden without calling the super method pretty often.
@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	@ModifyArg(
			method = "getEntityCollisions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/EntityGetter;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private Predicate<Entity> dontCollideWithDisintegratingEntities(Predicate<Entity> original) {
		return original.and(entity -> !Disintegration.isDisintegrating(entity));
	}
}
