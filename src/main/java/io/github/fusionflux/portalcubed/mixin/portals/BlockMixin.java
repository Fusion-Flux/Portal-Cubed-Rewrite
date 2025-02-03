package io.github.fusionflux.portalcubed.mixin.portals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

@Mixin(Block.class)
public class BlockMixin {
	@WrapOperation(
			method = "pushEntitiesUp",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;teleportRelative(DDD)V"
			)
	)
	private static void onPushUp(Entity instance, double offsetX, double offsetY, double offsetZ, Operation<Void> original) {
		original.call(instance, offsetX, offsetY, offsetZ);
		// teleportRelative calls teleportTo which assumes it's non-local
		// this is local, so undo that
		instance.pc$setNextTeleportNonLocal(false);
	}
}
