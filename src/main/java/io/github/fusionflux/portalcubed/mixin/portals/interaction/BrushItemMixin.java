package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(BrushItem.class)
public class BrushItemMixin {
	@WrapOperation(
			method = "calculateHitResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnViewVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/HitResult;"
			)
	)
	private HitResult raycastThroughPortals(Entity entity, Predicate<Entity> filter, double range, Operation<HitResult> original) {
		HitResult originalResult = original.call(entity, filter, range);

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.blocks(ClipContext.Block.COLLIDER)
				.entities(filter)
				.collisionContext(entity)
				.hitWorldBorder(true)
				.build();

		Vec3 start = entity.getEyePosition();
		// vanilla uses 0 here, not 1
		Vec3 direction = entity.getViewVector(0);
		RaycastResult.VanillaConvertible result = options.raycast(entity.level(), start, direction, range).assertNotPortal();
		return result.passedThroughPortals() ? result.toVanilla() : originalResult;
	}
}
