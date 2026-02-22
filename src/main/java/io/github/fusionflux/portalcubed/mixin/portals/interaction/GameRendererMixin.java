package io.github.fusionflux.portalcubed.mixin.portals.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.framework.raycast.RaycastOptions;
import io.github.fusionflux.portalcubed.framework.raycast.RaycastResult;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyReturnValue(
			method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
			at = @At("RETURN")
	)
	private HitResult raycastThroughPortals(HitResult original,
											@Local(argsOnly = true) Entity entity,
											@Local(argsOnly = true, ordinal = 0) double blockReach,
											@Local(argsOnly = true, ordinal = 1) double entityReach,
											@Local(ordinal = 0) Vec3 eyePos) {

		double maxRange = Math.max(blockReach, entityReach);
		Vec3 direction = eyePos.vectorTo(original.getLocation()).normalize();

		RaycastOptions options = RaycastOptions.DEFAULT.edit()
				.collisionContext(entity)
				.blockRange(blockReach)
				.entityRange(entityReach)
				.build();

		RaycastResult.VanillaConvertible result = options.raycast(entity.level(), eyePos, direction, maxRange).assertVanillaConvertible();
		return result.passedThroughPortals() ? result.toVanilla() : original;
	}
}
