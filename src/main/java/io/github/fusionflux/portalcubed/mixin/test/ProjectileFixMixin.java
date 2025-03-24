package io.github.fusionflux.portalcubed.mixin.test;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;

import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;

import net.minecraft.world.entity.projectile.FishingHook;

import net.minecraft.world.entity.projectile.LlamaSpit;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import net.minecraft.world.entity.projectile.ThrowableProjectile;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AbstractArrow.class, AbstractHurtingProjectile.class, FireworkRocketEntity.class, FishingHook.class, LlamaSpit.class, ShulkerBullet.class, ThrowableProjectile.class})
public abstract class ProjectileFixMixin extends Projectile {
	protected ProjectileFixMixin(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	/*@WrapOperation(
			method = "stepMoveAndHit",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setPos(Lnet/minecraft/world/phys/Vec3;)V",
					ordinal = 0
			),
			require = 0,
			expect = 0
	)
	private void portalCubed$projectilePortalFixArrows(AbstractArrow self, Vec3 pos, Operation<Void> original) {
		this.portalCubed$projectileFix(self,pos,original);
	}

	@Unique
	private void portalCubed$projectileFix(Projectile self, Vec3 pos, Operation<Void> original) {
		Vec3 oldPos = self.position();
		original.call(self, pos);
		if (PortalTeleportHandler.handle(self, oldPos)) {
			// need to update the values that were used to move to this new pos
			Vec3 newVel = this.getDeltaMovement();
			this.setDeltaMovement(newVel);
		}
	}*/
}
