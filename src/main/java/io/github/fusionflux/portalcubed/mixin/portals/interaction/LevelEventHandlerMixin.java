package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {
	@WrapOperation(
			method = "notifyNearbyEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
			)
	)
	private List<LivingEntity> findEntitiesThroughPortals(Level level, Class<LivingEntity> clazz, AABB area, Operation<List<LivingEntity>> original) {
		Set<LivingEntity> set = PortalInteractionUtils.getEntitiesOfClass(level, clazz, area);
		set.addAll(original.call(level, clazz, area));
		return new ArrayList<>(set);
	}
}
