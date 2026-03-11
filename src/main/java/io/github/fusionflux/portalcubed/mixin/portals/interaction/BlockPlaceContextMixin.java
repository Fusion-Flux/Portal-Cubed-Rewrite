package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.mixin.utils.accessors.UseOnContextAccessor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(BlockPlaceContext.class)
public abstract class BlockPlaceContextMixin extends UseOnContextMixin {
	@ModifyArg(
			method = "at",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/context/BlockPlaceContext;<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/BlockHitResult;)V"
			)
	)
	private static BlockHitResult preservePortalContext(BlockHitResult newResult, @Local(argsOnly = true) BlockPlaceContext originalContext) {
		BlockHitResult originalResult = ((UseOnContextAccessor) originalContext).invokeGetHitResult();
		newResult.setPortalPath(originalResult.portalPath());
		return newResult;
	}

	@ModifyReturnValue(method = { "getNearestLookingDirection", "getNearestLookingVerticalDirection" }, at = @At("RETURN"))
	private Direction teleportLookingDirections(Direction original) {
		return this.teleportDirection(original);
	}

	@ModifyReturnValue(method = "getNearestLookingDirections", at = @At("RETURN"))
	private Direction[] teleportLookingDirections(Direction[] original) {
		for (int i = 0; i < original.length; i++) {
			original[i] = this.teleportDirection(original[i]);
		}

		return original;
	}
}
