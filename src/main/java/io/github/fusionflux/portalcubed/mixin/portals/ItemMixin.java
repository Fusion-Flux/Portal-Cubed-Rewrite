package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.clip.PortalHitResult;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.ClipContextAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
	private static BlockHitResult raycastThroughPortals(Level level, ClipContext context, Operation<BlockHitResult> original) {
		BlockHitResult originalResult = original.call(level, context);
		Vec3 from = context.getFrom();
		PortalHitResult portalHit = level.portalManager().lookup().clip(from, context.getTo());
		if (portalHit == null || portalHit.isFartherThan(originalResult, from)) {
			return originalResult;
		}

		while (true) {
			Vec3 start = portalHit.exitHit();
			Vec3 end = switch (portalHit) {
				case PortalHitResult.Mid mid -> mid.next().hit();
				case PortalHitResult.Tail tail -> tail.end();
			};

			ClipContext stepContext = createStepContext(context, start, end);
			BlockHitResult hit = original.call(level, stepContext);
			if (hit.getType() != HitResult.Type.MISS)
				return hit;

			if (!(portalHit instanceof PortalHitResult.Mid mid)) {
				// end reached, whole raycast missed
				// hit will be a miss, since we checked for a hit already
				return hit;
			}

			portalHit = mid.next();
		}
	}

	@Unique
	private static ClipContext createStepContext(ClipContext original, Vec3 start, Vec3 end) {
		ClipContext.Block block = ((ClipContextAccessor) original).getBlock();
		ClipContext.Fluid fluid = ((ClipContextAccessor) original).getFluid();
		CollisionContext collisionContext = ((ClipContextAccessor) original).getCollisionContext();
		return new ClipContext(start, end, block, fluid, collisionContext);
	}
}
