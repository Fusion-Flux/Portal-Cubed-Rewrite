package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {
	@WrapOperation(
			method = "getPlayersWithContainerOpen",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private <T extends Entity> List<T> getOpenersThroughPortals(Level level, EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> predicate, Operation<List<T>> original) {
		Set<T> throughPortals = PortalInteractionUtils.getEntitiesThroughPortals(level, test, area, predicate);
		throughPortals.addAll(original.call(level, test, area, predicate));
		return new ArrayList<>(throughPortals);
	}
}
