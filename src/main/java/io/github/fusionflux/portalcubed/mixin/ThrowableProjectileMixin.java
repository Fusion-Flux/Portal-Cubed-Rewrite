package io.github.fusionflux.portalcubed.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.misc.Lemonade;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin extends Entity {
	@Unique
	private final boolean hasCollisions = (Object) this instanceof Lemonade;

	private ThrowableProjectileMixin(EntityType<?> variant, Level world) {
		super(variant, world);
	}

	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;checkInsideBlocks()V"))
	private boolean dontCheckInsideBlocksAgain(ThrowableProjectile instance) {
		return !hasCollisions;
	}

	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;setPos(DDD)V"))
	private boolean moveInsteadOfSetPos(ThrowableProjectile instance, double x, double y, double z) {
		if (hasCollisions) {
			this.move(MoverType.SELF, new Vec3(x - getX(), y - getY(), z - getZ()));
			if (this.onGround()) {
				Block blockAffectingMovement = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock();
				float friction = blockAffectingMovement.getFriction() * 0.91f;
				this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 1d, friction));
			}
			return false;
		}
		return true;
	}

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
