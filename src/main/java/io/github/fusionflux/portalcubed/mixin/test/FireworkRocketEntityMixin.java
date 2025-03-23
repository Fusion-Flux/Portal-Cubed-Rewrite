package io.github.fusionflux.portalcubed.mixin.test;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends Projectile {
	protected FireworkRocketEntityMixin(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	//Unlike the rest of the portal fixes, this is really only fixing for elytra boosting
	@WrapOperation(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;setPos(DDD)V",
					ordinal = 0
			)
	)
	public void portalCubed$projectilePortalFix(FireworkRocketEntity self, double x, double y, double z, Operation<Void> original) {
		Vec3 oldPos = self.position();
		original.call(self, x, y, z);
		if (PortalTeleportHandler.handle(self, oldPos)) {
			// need to update the values that were used to move to this new pos
			Vec3 newVel = this.getDeltaMovement();
			this.setDeltaMovement(newVel);
			//collide.set(this.collide(newVel));
		}
	}
}
