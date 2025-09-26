package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fusionflux.portalcubed.content.portal.PortalInstance;
import io.github.fusionflux.portalcubed.content.portal.collision.PortalCollisionUtils;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(CollisionGetter.class)
public interface CollisionGetterMixin {
	@Inject(method = "collidesWithSuffocatingBlock", at = @At("HEAD"), cancellable = true)
	default void dontSuffocateInPortals(@Nullable Entity entity, AABB box, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof Level level && !level.portalManager().containsActivePortals(box)) {
			cir.setReturnValue(false);
		}
	}

	// this basically mirrors the logic in EntityMixin
	@ModifyReturnValue(method = "noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Z)Z", at = @At("RETURN"))
	default boolean checkPortalCollision(boolean original, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) AABB bounds) {
		if (entity == null) {
			// don't know where to look
			return original;
		}

		if (!original) {
			// portal collision is additive, collision already found. nothing else to do
			return false;
		}

		// original is true beyond this point

		AABB portalArea = bounds.minmax(entity.getBoundingBox());
		List<PortalInstance.Holder> portals = PortalCollisionUtils.findRelevantPortalsFor(entity, portalArea);
		if (portals.isEmpty())
			return true;

		for (PortalInstance.Holder holder : portals) {
			PortalInstance portal = holder.portal();
			for (OBB box : portal.perimeterBoxes) {
				if (box.intersects(bounds)) {
					return false;
				}
			}

			MutableBoolean collision = new MutableBoolean(false);
			PortalCollisionUtils.forEachBoxOnOtherSide(entity, holder, bounds, box -> {
				if (box.intersects(bounds)) {
					collision.setTrue();
					// we can stop iterating early
					return false;
				}

				return true;
			});

			if (collision.booleanValue()) {
				return false;
			}
		}

		return true;
	}
}
