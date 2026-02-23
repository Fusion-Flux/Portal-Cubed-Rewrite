package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"
			)
	)
	private HitResult clipThroughPortals(Entity entity, double range, float partialTicks, boolean fluids, Operation<HitResult> original) {
		HitResult originalResult = original.call(entity, range, partialTicks, fluids);

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.entities(Optional.empty())
				.collisionContext(entity)
				.fluids(fluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE)
				.build();

		Vec3 start = entity.getEyePosition(partialTicks);
		Vec3 direction = entity.getViewVector(partialTicks);
		RaycastResult result = options.raycast(entity.level(), start, direction, range);
		if (!result.passedThroughPortals())
			return originalResult;

		if (!(result instanceof RaycastResult.BlockLike blockLike)) {
			throw new IllegalStateException("Result should be a BlockLike, not " + result);
		}

		return blockLike.toVanilla();
	}
}
