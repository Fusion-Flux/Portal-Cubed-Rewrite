package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(ShulkerBoxBlockEntity.class)
public final class ShulkerBoxBlockEntityMixin {
	@ModifyExpressionValue(
			method = "moveCollidedEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/monster/Shulker;getProgressDeltaAabb(FLnet/minecraft/core/Direction;FFLnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
			)
	)
	private AABB validatePortals(AABB area, @Local(argsOnly = true) Level level) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.portalManager().validatePortals(area);
		}

		return area;
	}
}
