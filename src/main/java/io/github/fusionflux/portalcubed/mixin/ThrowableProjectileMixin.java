package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.world.entity.projectile.ThrowableProjectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
public class ThrowableProjectileMixin {
	@WrapOperation(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;setPos(DDD)V"
			)
	)
	private void moveThroughPortals(ThrowableProjectile self, double x, double y, double z, Operation<Void> original) {
		PortalTeleportHandler.handle(self, x, y, z, original);
	}
}
