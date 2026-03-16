package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(Item.class)
public class ItemMixin {
	@WrapOperation(
			method = "getPlayerPOVHitResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"
			)
	)
	private static BlockHitResult raycastThroughPortals(Level level, ClipContext context, Operation<BlockHitResult> original,
														@Local(argsOnly = true) Player player) {
		BlockHitResult originalResult = original.call(level, context);

		RaycastOptions options = RaycastOptions.of(context).forPlayer(player).build();
		RaycastResult result = options.raycast(level, context.getFrom(), context.getTo());

		if (!(result instanceof RaycastResult.BlockLike blockLike)) {
			throw new IllegalStateException("RaycastResult should be a BlockLike, was " + result);
		}

		// only override the default if portals were passed through, the result should be the same
		return blockLike.path.isEmpty() ? originalResult : blockLike.toVanilla();
	}
}
