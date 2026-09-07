package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.phys.AABB;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {
	@WrapOperation(
			method = "getEntitiesWithContainerOpen",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private List<Entity> getOpenersThroughPortals(Level level, @Nullable Entity except, AABB area, Predicate<? super Entity> selector,Operation<List<Entity>> original) {
		Set<Entity> throughPortals = PortalInteractionUtils.getEntities(level, except, area, selector);
		throughPortals.addAll(original.call(level, except, area, selector));
		return new ArrayList<>(throughPortals);
	}
}
