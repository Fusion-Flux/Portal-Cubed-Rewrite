package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@ModifyReturnValue(
			method = "pick", at = @At("RETURN"),
			allow = 2, require = 2 // ternary generates 2 separate returns
	)
	private static HitResult raycastThroughPortals(HitResult original, Entity entity, double blockReach, double entityReach, float partialTicks,
												   @Local(name = "from") Vec3 eyePos, @Local(name = "direction") Vec3 direction) {
		double maxRange = Math.max(blockReach, entityReach);
		PortalMode portalMode = shouldSelectPortals(entity) ? PortalMode.HIT : PortalMode.PASS_THROUGH;

		// we have to recalculate this instead of grabbing it with @Local since it's technically out of scope at the second return
		// Vec3 direction = entity.getViewVector(partialTicks);

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.portals(portalMode)
				.collisionContext(entity)
				.blockRange(blockReach)
				.entityRange(entityReach)
				.build();

		Minecraft mc = Minecraft.getInstance();
		mc.setSelectedPortal(null);

		RaycastResult result = options.raycast(entity.level(), eyePos, direction, maxRange);
		if (!preferOverOriginal(result))
			return original;

		return switch (result) {
			case RaycastResult.VanillaConvertible vanillaConvertible -> vanillaConvertible.toVanilla();
			case RaycastResult.Portal portal -> {
				mc.setSelectedPortal(portal);
				yield PortalInteractionUtils.convertToMiss(original, direction);
			}
		};
	}

	@Unique
	private static boolean preferOverOriginal(RaycastResult result) {
		if (result.passedThroughPortals() || result instanceof RaycastResult.Portal)
			return true;

		if (result instanceof RaycastResult.Entity entity) {
			return entity.isProxy();
		}

		return false;
	}

	@Unique
	private static boolean shouldSelectPortals(Entity entity) {
		if (!(entity instanceof LivingEntity living))
			return false;

		return living.isHolding(stack -> stack.getItem() instanceof UsableOnPortals);
	}
}
