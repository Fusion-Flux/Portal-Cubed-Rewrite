package io.github.fusionflux.portalcubed.mixin.portals;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}
