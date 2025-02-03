package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

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

		level.portalManager().removePortalsInBox(aabb);
		return original.call(level, aClass, aabb, predicate);
	}
}
