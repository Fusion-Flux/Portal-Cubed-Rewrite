package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.PortalReference;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;

@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	@ModifyArg(
			method = "getEntityCollisions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/EntityGetter;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private Predicate<Entity> carveEntities(Predicate<Entity> predicate, @Local(argsOnly = true) @Nullable Entity entity) {
		if (entity == null) {
			return predicate;
		}

		Set<PortalReference> portals = entity.relevantPortals().get();
		if (portals.isEmpty())
			return predicate;

		return predicate.and(otherEntity -> {
			AABB box = otherEntity.getBoundingBox();

			for (PortalReference portal : portals) {
				if (portal.get().hides(box)) {
					return false;
				}
			}

			return true;
		});
	}
}
