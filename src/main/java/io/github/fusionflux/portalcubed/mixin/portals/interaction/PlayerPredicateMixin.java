package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(PlayerPredicate.class)
public class PlayerPredicateMixin {
	@WrapOperation(
			method = "matches",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;"
			)
	)
	private @Nullable EntityHitResult hitEntitiesThroughPortals(Level level, Entity entity, Vec3 from, Vec3 to, AABB area, Predicate<Entity> filter, float expand, Operation<EntityHitResult> original) {
		EntityHitResult originalResult = original.call(level, entity, from, to, area, filter, expand);
		RaycastResult result = RaycastOptions.forEntitiesOnly(entity, filter, expand).build().raycast(level, from, to);
		if (!result.passedThroughPortals() || !(result instanceof RaycastResult.Entity entityResult))
			return originalResult;

		return entityResult.toVanilla();
	}
}
