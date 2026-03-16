package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Shadow
	@Final
	private static double MAX_LINE_OF_SIGHT_TEST_RANGE;

	@ModifyExpressionValue(
			method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"
			)
	)
	private double skipDistanceCheck(double distance) {
		return 0;
	}

	@WrapOperation(
			method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"
			)
	)
	private BlockHitResult raycastThroughPortals(Level level, ClipContext context, Operation<BlockHitResult> original,
												 @Cancellable CallbackInfoReturnable<Boolean> cir) {
		BlockHitResult originalResult = original.call(level, context);
		Vec3 from = context.getFrom();
		Vec3 to = context.getTo();

		if (originalResult.getType() != HitResult.Type.MISS) {
			// no line of sight normally, check through portals
			RaycastResult result = RaycastOptions.of(context).build().raycast(level, from, to);
			if (result.passedThroughPortals() && result instanceof RaycastResult.Missed missed) {
				// we passed through portals and hit nothing, override the original
				double distance = missed.path.orElseThrow().distanceThrough(from, to);
				if (distance > MAX_LINE_OF_SIGHT_TEST_RANGE) {
					// too far, cancel
					cir.setReturnValue(false);
					return originalResult;
				} else {
					return missed.toVanilla();
				}
			}
		}

		// fall back to vanilla, but we need to do that distance check we skipped earlier
		double distance = from.distanceTo(to);
		if (distance > MAX_LINE_OF_SIGHT_TEST_RANGE) {
			cir.setReturnValue(false);
		}

		return originalResult;
	}
}
