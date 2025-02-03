package io.github.fusionflux.portalcubed.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;

@Mixin(Entity.class)
public class EntityMixin {
	@ModifyArg(
			method = "pick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"
			)
	)
	private ClipContext crowbarIgnoresInteractionOverride(ClipContext ctx) {
		//noinspection ConstantValue
		if ((Object) this instanceof LivingEntity living && living.getMainHandItem().is(PortalCubedItems.CROWBAR)) {
			ctx.pc$setIgnoreInteractionOverride(true);
		}

		return ctx;
	}
}
