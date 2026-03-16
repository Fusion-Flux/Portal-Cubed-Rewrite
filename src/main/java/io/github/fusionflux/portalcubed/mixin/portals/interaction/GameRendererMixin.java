package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.interaction.PortalInteractionUtils;
import io.github.fusionflux.portalcubed.content.portal.interaction.UsableOnPortals;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions.PortalMode;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyReturnValue(
			method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
			at = @At("RETURN"), require = 2 // ternary generates 2 separate returns
	)
	private HitResult raycastThroughPortals(HitResult original, Entity entity, double blockReach, double entityReach,
											float partialTicks, @Local(ordinal = 0) Vec3 eyePos) {
		double maxRange = Math.max(blockReach, entityReach);
		PortalMode portalMode = shouldSelectPortals(entity) ? PortalMode.HIT : PortalMode.PASS_THROUGH;

		// we have to recalculate this instead of grabbing it with @Local since it's technically out of scope at the second return
		Vec3 direction = entity.getViewVector(partialTicks);

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.portals(portalMode)
				.collisionContext(entity)
				.blockRange(blockReach)
				.entityRange(entityReach)
				.build();

		this.minecraft.setSelectedPortal(null);
		RaycastResult result = options.raycast(entity.level(), eyePos, direction, maxRange);
		if (!result.passedThroughPortals() && !(result instanceof RaycastResult.Portal))
			return original;

		return switch (result) {
			case RaycastResult.VanillaConvertible vanillaConvertible -> vanillaConvertible.toVanilla();
			case RaycastResult.Portal portal -> {
				this.minecraft.setSelectedPortal(portal);
				yield PortalInteractionUtils.convertToMiss(original, direction);
			}
		};
	}

	@Unique
	private static boolean shouldSelectPortals(Entity entity) {
		if (!(entity instanceof LivingEntity living))
			return false;

		return living.isHolding(stack -> stack.getItem() instanceof UsableOnPortals);
	}
}
