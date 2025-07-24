package io.github.fusionflux.portalcubed.mixin.portals.projectile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fusionflux.portalcubed.content.portal.PortalTeleportHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
	protected AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(
			method = "stepMoveAndHit",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setPos(Lnet/minecraft/world/phys/Vec3;)V",
					ordinal = 0
			)
	)
	private void allowArrowsThroughPortals(AbstractArrow self, Vec3 pos, Operation<Void> original) {
		Vec3 oldPos = self.position();
		original.call(self, pos);
		PortalTeleportHandler.handle(self, oldPos);
	}
}
