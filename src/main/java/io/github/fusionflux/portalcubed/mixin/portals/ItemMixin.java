package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.ClipContextAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

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

		ClipContext.Block blockMode = ((ClipContextAccessor) context).getBlock();
		ClipContext.Fluid fluidMode = ((ClipContextAccessor) context).getFluid();
		CollisionContext collisionContext = ((ClipContextAccessor) context).getCollisionContext();

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.entities(Optional.empty())
				.blocks(blockMode)
				.fluids(fluidMode)
				.forPlayer(player)
				.collisionContext(collisionContext)
				.build();

		RaycastResult result = options.raycast(level, context.getFrom(), context.getTo());
		if (!(result instanceof RaycastResult.BlockLike blockLike)) {
			throw new IllegalStateException("RaycastResult should be a BlockLike, was " + result);
		}

		// only override the default if portals were passed through, the result should be the same
		return blockLike.path.isEmpty() ? originalResult : blockLike.toVanilla();
	}
}
