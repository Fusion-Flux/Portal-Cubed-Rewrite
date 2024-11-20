package io.github.fusionflux.portalcubed.mixin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.PortalPair;

import io.github.fusionflux.portalcubed.content.portal.PortalType;

import io.github.fusionflux.portalcubed.content.portal.manager.ServerPortalManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Mixin(StructureUtils.class)
public class StructureUtilsMixin {
	@WrapOperation(
			method = "clearSpaceForStructure",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
			)
	)
	private static <T extends Entity> List<T> clearPortals(ServerLevel level, Class<T> aClass, AABB aabb,
														   Predicate<? super T> predicate, Operation<List<T>> original) {
		// TODO: this is a mess. Need a section-based lookup and easy removal
		ServerPortalManager manager = level.portalManager();
		// copy the ID set to avoid a CME
		for (UUID key : new HashSet<>(manager.getAllIds())) {
			manager.modifyPair(key, pair -> {
				if (pair.primary().isPresent()) {
					PortalInstance primary = pair.primary().get();
					if (primary.renderBounds.intersects(aabb)) {
						pair = pair.without(PortalType.PRIMARY);
					}
				}
				if (pair.secondary().isPresent()) {
					PortalInstance secondary = pair.secondary().get();
					if (secondary.renderBounds.intersects(aabb)) {
						pair = pair.without(PortalType.SECONDARY);
					}
				}

				return pair;
			});
		}

		return original.call(level, aClass, aabb, predicate);
	}
}
