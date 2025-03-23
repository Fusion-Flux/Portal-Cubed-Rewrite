package io.github.fusionflux.portalcubed.mixin.test;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin extends Projectile {

	protected AbstractHurtingProjectileMixin(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;setPos(Lnet/minecraft/world/phys/Vec3;)V",
					ordinal = 0
			)
	)
	public void portalCubed$projectilePortalFix(AbstractHurtingProjectile self, Vec3 pos, Operation<Void> original) {
		Vec3 oldPos = self.position();
		original.call(self, pos);
		if (PortalTeleportHandler.handle(self, oldPos)) {
			// need to update the values that were used to move to this new pos
			Vec3 newVel = this.getDeltaMovement();
			this.setDeltaMovement(newVel);
			//collide.set(this.collide(newVel));
		}
	}
}
